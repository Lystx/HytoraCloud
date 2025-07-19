package cloud.hytora.node.service.helper;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.progressbar.HytoraProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.entity.services.impl.UniversalCloudServer;
import cloud.hytora.driver.entity.services.task.TaskDownloadEntry;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.RemoteIdentity;
import cloud.hytora.driver.entity.services.utils.ServiceProcessType;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.driver.entity.services.utils.version.VersionFile;
import cloud.hytora.driver.entity.services.utils.version.VersionType;
import cloud.hytora.driver.config.def.UniversalNetworkConfig;
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
    public Task<CloudService> processService(CloudService service) {
        Task<CloudService> task = Task.empty(CloudService.class).denyNull();


        service.setServiceState(ServiceState.STARTING);
        service.update(PublishingType.GLOBAL);

        // add statistic to service
        NodeDriver.getInstance().getExecutor().registerStats(service);

        downloadServiceVersion(service.getTask().getVersion());

        // create server dir
        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? CloudDriver.Constants.SERVICE_DIR_STATIC : CloudDriver.Constants.SERVICE_DIR_DYNAMIC);


        File serverDir = CloudDriver.getInstance().getServerDirectoryFormatter().supply(Tuple.of(parent, service));
        FileUtils.forceMkdirParent(serverDir);

        FileUtils.forceMkdir(serverDir);

        // load all current task templates
        ServiceTask serviceTask = service.getTask();
        ServiceProcessType serviceProcessType = UniversalNetworkConfig.getInstance().getServiceProcessType();

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
        FileUtils.copyFile(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, jar), new File(serverDir, jar));

        // copy plugin
        FileUtils.copyFile(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER,  CloudDriver.Constants.BRIDGE_FILE_NAME), new File(serverDir, "plugins/"  + CloudDriver.Constants.BRIDGE_FILE_NAME));

        if (serviceProcessType == ServiceProcessType.WRAPPER) {
            //copy remote file
            FileUtils.copyFile(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, CloudDriver.Constants.REMOTE_FILE_NAME), new File(serverDir, CloudDriver.Constants.REMOTE_FILE_NAME));
        }

        // write property for identify service
        new RemoteIdentity(
                NodeDriver.getInstance().getNode().getConfig().getAuthKey(),
                service.getRunningNodeName(),
                service.getTask().getVersion().getType(),
                UniversalNetworkConfig.getInstance().getServiceProcessType(),
                CloudDriver.getInstance().getLogger().getMinLevel(),
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
            if (config.getCopyType().applies(serviceTask.getTaskGroup().getEnvironment()) || (config.getCopyType() == ModuleCopyType.SERVER_FALLBACK && service.getTask().getFallback().isEnabled())) {
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


        if (service.getDefaultWorld() != null && !service.getDefaultWorld().equalsIgnoreCase("world")) {
            FileUtils.deleteDirectory(new File(serverDir, "world"));
            String defaultWorld = service.getDefaultWorld();
            File file = new File(CloudDriver.Constants.TEMPLATES_DIR, defaultWorld);
            FileUtils.copyFile(file, new File(serverDir, "world"));
        }

        File folder = CloudDriver.getInstance().getServerDirectoryFormatter().supply(Tuple.of(parent, service));

        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);

        StartedProcess result = new ProcessExecutor()
                .command(this.args(service))
                .directory(folder)
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {
                        Screen screenByNameOrNull = screenManager.getCachedScreen(service.getName());
                        screenByNameOrNull.writeLine(line);

                    }
                })
                .start();

        Process process = result.getProcess();

        UniversalCloudServer serviceInfo = (UniversalCloudServer)service;
        serviceInfo.setProcess(process);
        serviceInfo.setWorkingDirectory(folder);
        service.update(PublishingType.INTERNAL);

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

    private String[] args(CloudService service) throws IOException {

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? CloudDriver.Constants.SERVICE_DIR_STATIC : CloudDriver.Constants.SERVICE_DIR_DYNAMIC);
        File folder = CloudDriver.getInstance().getServerDirectoryFormatter().supply(Tuple.of(parent, service));
        Path remoteFile1 = new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER,  CloudDriver.Constants.REMOTE_FILE_NAME).toPath();
        FileUtils.copyFile(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, CloudDriver.Constants.REMOTE_FILE_NAME), new File(folder, CloudDriver.Constants.REMOTE_FILE_NAME));
        File applicationFile = new File(folder, service.getTask().getVersion().getJar());

        Path remoteFile  = new File(folder, CloudDriver.Constants.REMOTE_FILE_NAME).toPath();
        ServiceTask task = service.getTask();
        int javaVersion = task.getJavaVersion();
        ServiceProcessType serviceProcessType = UniversalNetworkConfig.getInstance().getServiceProcessType();

        List<String> arguments = new ArrayList<>(Collections.singletonList("java"));

        if (javaVersion != -1) {
            // TODO: 16.07.2025 multi java -> inspiration look at cloudnet 
            UniversalNetworkConfig.getInstance().getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().ifPresent(version -> arguments.add(version.getPath()));
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


    private Task<Boolean> downloadServiceVersion(ServiceVersion version) {
        File file = new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, version.getJar());

        if (file.exists()) {
            return Task.build(true);
        }

        CloudDriver.getInstance().getLogger().info("§6=> §7Requiring to download %1" + version.getJar() + "§8!");

        file.getParentFile().mkdirs();


        Task<Boolean> task = Task.empty();

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
                FileUtils.copyFile(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, "cache/patched_" + version.getVersion() + ".jar"), file);
                FileUtils.deleteDirectory(new File(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER, "cache/"));
            }
        } catch (IOException e) {
            e.printStackTrace();
            task.setFailure(e);
            CloudDriver.getInstance().getLogger().error("§cFailed to download version§7... (%2" + version.getTitle() + "§7)");
        }
        task.setResult(true);
        CloudDriver.getInstance().getLogger().info("§a=> §7Downloaded %1" + version.getJar() + "§8!");
        return task;
    }


}
