package cloud.hytora.node.service.helper;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRequestScreenLeaveEvent;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.driver.services.utils.version.VersionFile;
import cloud.hytora.driver.services.utils.version.VersionType;
import cloud.hytora.node.console.progressbar.ProgressBar;
import cloud.hytora.node.console.progressbar.ProgressBarStyle;
import cloud.hytora.node.impl.config.MainConfiguration;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.NodeDriver;


import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;


public class CloudServerProcessWorker {

    @SneakyThrows
    public Task<ServiceInfo> processService(ServiceInfo service) {
        Task<ServiceInfo> task = Task.empty(ServiceInfo.class).denyNull();


        service.setServiceState(ServiceState.STARTING);

        // add statistic to service
        NodeDriver.getInstance().getExecutor().registerStats(service);

        downloadServiceVersion(service.getTask().getVersion());

        // create server dir
        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serverDir = new File(parent, service.getName() + "/");

        FileUtils.forceMkdir(serverDir);

        // load all current task templates
        ServiceTask serviceTask = service.getTask();
        ServiceProcessType serviceProcessType = MainConfiguration.getInstance().getServiceProcessType();

        //all templates for this service
        Collection<ServiceTemplate> templates = serviceTask.getTaskGroup().getTemplates(); //parent templates
        templates.addAll(serviceTask.getTemplates()); //task templates

        for (ServiceTemplate template : templates) {
            TemplateStorage storage = template.getStorage();
            if (storage != null) {
                storage.copyTemplate(service, template, serverDir);
            }
        }

        String jar = service.getTask().getVersion().getJar();
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, jar), new File(serverDir, jar));

        // copy plugin
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "plugin.jar"), new File(serverDir, "plugins/plugin.jar"));

        if (serviceProcessType == ServiceProcessType.WRAPPER) {
            //copy remote file
            FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar"), new File(serverDir, "remote.jar"));
        }

        // write property for identify service
        new RemoteIdentity(
                NodeDriver.getInstance().getConfig().getAuthKey(),
                service.getTask().getNode(),
                service.getTask().getVersion().getType(),
                MainConfiguration.getInstance().getServiceProcessType(),
                NodeDriver.getInstance().getExecutor().getHostName(),
                service.getName(),
                NodeDriver.getInstance().getExecutor().getPort()
        ).save(new File(serverDir, "property.json"));

        //copy extra downloads
        for (TaskDownloadEntry entry : service.getTask().getTaskGroup().getDownloadEntries()) {
            CloudDriver.getInstance().getLogger().log(LogLevel.INFO, "Downloading entry for '{}' [url={}, dest={}]", service.getName(), entry.getUrl(), entry.getDestination());
            String url = entry.getUrl();
            FileUtils.copyURLToFile(new URL(url), new File(serverDir, entry.getDestination()));
        }

        //copying modules
        for (ModuleController module : CloudDriver.getInstance().getModuleManager().getModules()) {
            ModuleConfig config = module.getModuleConfig();
            if (config.getCopyType().applies(serviceTask.getTaskGroup().getEnvironment())) {
                Path jarFile = module.getJarFile();
                FileUtils.copyFile(jarFile.toFile(), new File(new File(serverDir, "plugins/"), jarFile.toFile().getName()));
            }
        }

        ServiceVersion version = service.getTask().getVersion();

        //managing version specific files
        for (VersionFile versionFile : version.instantiateVersionFiles()) {
            File file = new File(serverDir, versionFile.getFileName());
            versionFile.applyFile(service, file);
        }

        File folder = new File(parent, service.getName() + "/");


        ScreenManager screenManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);

        StartedProcess result = new ProcessExecutor()
                .command(this.args(service))
                .directory(folder)
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        Screen screenByNameOrNull = screenManager.getScreenByNameOrNull(service.getName());
                        screenByNameOrNull.writeLine(line);

                    }
                })
                .start();

        Process process = result.getProcess();

        SimpleServiceInfo serviceInfo = (SimpleServiceInfo)service;
        serviceInfo.setProcess(process);
        serviceInfo.setWorkingDirectory(folder);

        task.setResult(serviceInfo);

        return task;
    }

    public boolean shouldPreloadClassesBeforeStartup(Path applicationFile) {
        try (JarFile file = new JarFile(applicationFile.toFile())) {
            return file.getEntry("META-INF/versions.list") != null;
        } catch (IOException exception) {
            // wtf?
            return false;
        }
    }

    public String getMainClass(Path applicationFile) {
        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(applicationFile))) {
            return jarInputStream.getManifest().getMainAttributes().getValue("Main-Class");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    private String[] args(ServiceInfo service) {

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        Path remoteFile = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar").toPath();
        File applicationFile = new File(folder, service.getTask().getVersion().getJar());

        ServiceTask task = service.getTask();
        int javaVersion = task.getJavaVersion();
        ServiceProcessType serviceProcessType = MainConfiguration.getInstance().getServiceProcessType();

        List<String> arguments = new ArrayList<>(Collections.singletonList("java"));

        if (javaVersion != -1) {

            INodeConfig config = NodeDriver.getInstance().getConfig();
            config.getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().ifPresent(version -> arguments.add(version.getPath()));

        }

        //adding pre defined arguments
        arguments.addAll(
                Arrays.asList(
                        "-DIReallyKnowWhatIAmDoingISwear",
                        "-Dcom.mojang.eula.agree=true",
                        "-Xmx" + service.getTask().getMemory() + "M"
                )
        );

        if (serviceProcessType == ServiceProcessType.WRAPPER) {
            arguments.add("-javaagent:" + remoteFile.toAbsolutePath());
            // forces the vm to add the wrapper jar to the classpath (ucp) of the builtin boot classloader
        }


        if (task.getJavaVersion() >= 9) {
            arguments.addAll(Arrays.asList(
                    // was earlier needed to be able to access the private ucp field of the builtin classloader in java9+
                    // we leave it in for the case we or some plugins want to do some pre-java9-like reflections
                    "--add-opens", "java.base/jdk.internal.loader=ALL-UNNAMED"
            ));
        }

        //adding custom task arguments
        if (task.getTaskGroup().getJavaArguments() != null && task.getTaskGroup().getJavaArguments().length > 0) {
            arguments.addAll(Arrays.asList(task.getTaskGroup().getJavaArguments()));
        }

        if (serviceProcessType == ServiceProcessType.WRAPPER) {
            arguments.addAll(Arrays.asList("-cp", remoteFile.toAbsolutePath() + ":" + applicationFile.toPath().toAbsolutePath()));

            String mainClass = getMainClass(applicationFile.toPath());
            String remoteMainClass = getMainClass(remoteFile);

            if (mainClass == null || remoteMainClass == null) {
                System.out.println("MASSIVE ERROR");
                return null;
            }

            arguments.add(remoteMainClass);
            //arguments.add(mainClass);
            arguments.add(applicationFile.getName());
            
        } else if (serviceProcessType == ServiceProcessType.BRIDGE_PLUGIN) {
            arguments.addAll(Arrays.asList("-jar", applicationFile.getName()));
        }

        if (service.getTask().getVersion().getType() == VersionType.SPIGOT) {
            arguments.add("nogui");
        }


        return arguments.toArray(new String[0]);
    }


    private void downloadServiceVersion(ServiceVersion version) {
        File file = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, version.getJar());

        if (file.exists()) {
            return;
        }

        CloudDriver.getInstance().getLogger().info("§6=> §7Requiring to download §b" + version.getJar() + "§8!");

        file.getParentFile().mkdirs();

        try {
            String url = version.getUrl();
            FileUtils.copyURLToFile(new URL(url), file);

            if (version.getTitle().equals("paper")) {
                Process process = new ProcessBuilder("java", "-jar", version.getJar()).directory(file.getParentFile()).start();
                InputStreamReader inputStreamReader = new InputStreamReader(process.getInputStream());
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                bufferedReader.readLine();
                bufferedReader.readLine();
                bufferedReader.readLine();
                process.destroyForcibly();
                bufferedReader.close();
                inputStreamReader.close();
                FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "cache/patched_" + version.getVersion() + ".jar"), file);
                FileUtils.deleteDirectory(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "cache/"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            CloudDriver.getInstance().getLogger().error("§cFailed to download version§7... (§3" + version.getTitle() + "§7)");
            return;
        }
        CloudDriver.getInstance().getLogger().info("§a=> §7Downloaded §b" + version.getJar() + "§8!");
    }


}
