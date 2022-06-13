package cloud.hytora.node.service.helper;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.wrapper.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerRequestScreenLeaveEvent;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.driver.services.utils.version.VersionFile;
import cloud.hytora.node.impl.event.ServiceOutputLineAddEvent;
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
import java.util.jar.JarInputStream;


public class ServiceQueueProcessWorker {

    private final ServiceInfo service;
    private final NodeServiceManager serviceManager;


    @SneakyThrows
    public ServiceQueueProcessWorker(NodeServiceManager serviceManager, ServiceInfo service) {
        this.serviceManager = serviceManager;
        this.service = service;
        this.service.setServiceState(ServiceState.STARTING);

        // add statistic to service
        NodeDriver.getInstance().getExecutor().registerStats(this.service);

        this.downloadServiceVersion(this.service.getTask().getVersion());

        // create server dir
        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serverDir = new File(parent, service.getName() + "/");

        FileUtils.forceMkdir(serverDir);

        // load all current task templates
        ServiceTask serviceTask = service.getTask();

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

        // write property for identify service
        new RemoteIdentity(
                NodeDriver.getInstance().getConfig().getAuthKey(),
                service.getTask().getNode(),
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

        File serverIcon = new File(serverDir, "server-icon.png");
        if (!serverIcon.exists()) {
            copyFileWithURL("/impl/files/server-icon.png", new File(serverDir, "server-icon.png")); //copying server icon if none already provided
        }

        //managing version specific files
        for (VersionFile versionFile : version.instantiateVersionFiles()) {
            File file = new File(serverDir, versionFile.getFileName());
            versionFile.applyFile(service, file);
        }

    }

    @SneakyThrows
    public Task<ServiceInfo> processService() {
        Task<ServiceInfo> task = Task.empty(ServiceInfo.class).denyNull();

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        StartedProcess result = new ProcessExecutor()
                .command(this.args(this.service))
                .directory(folder)
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {

                        DestructiveListener listener = CloudDriver.getInstance().getEventManager().registerSelfDestructiveHandler(ServiceOutputLineAddEvent.class, event -> {
                            String line1 = event.getLine();
                            if (serviceManager.getCachedServiceOutputs().get(service.getName()) == null) {
                                return;
                            }
                            serviceManager.getCachedServiceOutputs().get(service.getName()).add(line1);
                            if (service.asCloudServer().isScreenServer()) {
                                CloudDriver.getInstance().getCommandSender().sendMessage(line1);
                            }
                        });
                        CloudDriver.getInstance().getEventManager().registerSelfDestructiveHandler(CloudServerCacheUnregisterEvent.class, e -> {
                            if (service.asCloudServer().isScreenServer()) {
                                CloudDriver.getInstance().getEventManager().callEventGlobally(new CloudServerRequestScreenLeaveEvent(CloudDriver.getInstance().getCommandManager(), CloudDriver.getInstance().getConsole(), CloudDriver.getInstance().getCommandSender(), service));
                            }
                            listener.destroy();
                        });

                        CloudDriver.getInstance().getEventManager().callEventGlobally(new ServiceOutputLineAddEvent(service, line));
                    }
                })
                .start();

        Process process = result.getProcess();
        this.service.asCloudServer().setProcess(process);
        this.service.asCloudServer().setWorkingDirectory(folder);
        task.setResult(this.service);

        return task;
    }


    private String[] args(ServiceInfo service) {
        ServiceTask task = service.getTask();
        List<String> arguments = new ArrayList<>(Arrays.asList("java"));
        int javaVersion = task.getJavaVersion();


        if (javaVersion != -1) {

            INodeConfig config = NodeDriver.getInstance().getConfig();
            JavaVersion version = config.getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().orElse(null);

            if (version != null) {
                arguments.add(version.getPath()); //adding path to custom java version
            }
        }

        //adding pre defined arguments
        arguments.addAll(Arrays.asList(
                "-DIReallyKnowWhatIAmDoingISwear",
                "-Dcom.mojang.eula.agree=true",
                "-Xms" + service.getTask().getMemory() + "M",
                "-Xmx" + service.getTask().getMemory() + "M")
        );

        //adding custom task arguments
        if (task.getTaskGroup().getJavaArguments() != null && task.getTaskGroup().getJavaArguments().length > 0) {
            arguments.addAll(Arrays.asList(task.getTaskGroup().getJavaArguments()));
        }

        Path remoteFile = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar").toPath();

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File applicationFile = new File(parent, service.getName() + "/" + service.getTask().getVersion().getJar());


        arguments.addAll(Arrays.asList(
                "-cp", remoteFile.toAbsolutePath() + File.pathSeparator + applicationFile.toPath().toAbsolutePath()));

        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(remoteFile))) {
            arguments.add(jarInputStream.getManifest().getMainAttributes().getValue("Main-Class"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (JarInputStream jarInputStream = new JarInputStream(Files.newInputStream(applicationFile.toPath()))) {
            arguments.add(jarInputStream.getManifest().getMainAttributes().getValue("Main-Class"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        if (service.getTask().getVersion().getEnvironment() == SpecificDriverEnvironment.MINECRAFT) {
            arguments.add("nogui");
        }

        return arguments.toArray(new String[]{});
    }


    private void copyFileWithURL(String filename, File location) {
        try {
            URL inputUrl = getClass().getResource(filename);
            if (location.exists()) {
                return;
            }
            if (inputUrl == null) {
                throw new CloudException("Couldn't copy " + filename + " to Location: " + location.toString());
            }
            try {
                FileUtils.copyURLToFile(inputUrl, location);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }


    private void downloadServiceVersion(ServiceVersion version) {
        File file = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, version.getJar());

        if (file.exists()) {
            return;
        }

        CloudDriver.getInstance().getLogger().info("§7Downloading §bVersion§7... (§3" + version.getTitle() + "§7)");

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
        CloudDriver.getInstance().getLogger().info("Downloading of (§3" + version.getTitle() + "§7)§a successfully §7completed.");
    }


}
