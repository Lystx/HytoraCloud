package cloud.hytora.node.service.helper;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Console;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.template.ITemplateStorage;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.driver.services.utils.version.VersionFile;
import cloud.hytora.driver.services.utils.version.VersionType;
import cloud.hytora.node.config.MainConfiguration;
import cloud.hytora.node.NodeDriver;


import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.zeroturnaround.exec.ProcessExecutor;
import org.zeroturnaround.exec.StartedProcess;
import org.zeroturnaround.exec.stream.LogOutputStream;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;


public class CloudServerProcessWorker {

    @SneakyThrows
    public ITask<ICloudServer> processService(ICloudServer service) {
        ITask<ICloudServer> task = ITask.empty();
        ITask.runAsync(() -> {

            service.setServiceState(ServiceState.STARTING);

            // add statistic to service
            NodeDriver.getInstance().getNetworkExecutor().registerStats(service);

            ServiceVersion serviceVersion = service.getTask().getVersion();
            this.downloadVersion(serviceVersion)
                    .onTaskFailed(e -> {
                        CloudDriver.getInstance().getLogger().error("§cFailed to download version§7... (§3" + serviceVersion.getTitle() + "§7)");
                    })
                    .onTaskSucess((ExceptionallyConsumer<Boolean>) n -> {
                        if (n) {
                            Logger.constantInstance().info("§a=> §7Downloaded §b" + serviceVersion.getJar() + "§8!");
                        }
                        // create server dir
                        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
                        File serverDir = new File(parent, service.getName() + "/");

                        FileUtils.forceMkdir(serverDir);

                        // load all current task templates
                        IServiceTask serviceTask = service.getTask();
                        ServiceProcessType serviceProcessType = MainConfiguration.getInstance().getServiceProcessType();

                        //all templates for this service
                        Collection<ITemplate> templates = serviceTask.getTaskGroup().getTemplates(); //parent templates
                        templates.addAll(serviceTask.getTemplates()); //task templates

                        for (ITemplate template : templates) {
                            ITemplateStorage storage = template.getStorage();
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
                                NodeDriver.getInstance().getNode().getConfig().getAuthKey(),
                                service.getRunningNodeName(),
                                service.getTask().getVersion().getType(),
                                MainConfiguration.getInstance().getServiceProcessType(),
                                CloudDriver.getInstance().getLogger().getMinLevel(),
                                MainConfiguration.getInstance().getPlayerLoginProcessing(),
                                NodeDriver.getInstance().getNetworkExecutor().getHostName(),
                                service.getName(),
                                NodeDriver.getInstance().getNetworkExecutor().getPort()
                        ).save(new File(serverDir, "property.json"));

                        //copy extra downloads
                        for (TaskDownloadEntry entry : service.getTask().getTaskGroup().getDownloadEntries()) {
                            CloudDriver.getInstance().getLogger().log(LogLevel.INFO, "Downloading entry for '{}' [url={}, dest={}]", service.getName(), entry.getUrl(), entry.getDestination());
                            String url = entry.getUrl();
                            FileUtils.copyURLToFile(new URL(url), new File(serverDir, entry.getDestination()));
                        }

                        //copying modules
                        for (ModuleController module : CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class).getModules()) {
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

                        File folder = new File(parent, service.getName() + "/");

                        CloudDriver
                                .getInstance()
                                .getProviderRegistry()
                                .get(ScreenManager.class)
                                .ifPresent((ExceptionallyConsumer<ScreenManager>)screenManager -> {

                                    StartedProcess result = new ProcessExecutor()
                                            .command(this.args(service))
                                            .directory(folder)
                                            .redirectOutput(new LogOutputStream() {
                                                @Override
                                                protected void processLine(String line) {
                                                    Screen serviceScreen = screenManager.getScreenByNameOrNull(service.getName());
                                                    serviceScreen.writeLine(line);
                                                }
                                            })
                                            .start();

                                    Process process = result.getProcess();

                                    UniversalCloudServer serviceInfo = (UniversalCloudServer) service;
                                    serviceInfo.setProcess(process);
                                    serviceInfo.setWorkingDirectory(folder);


                                    task.setResult(serviceInfo);

                                });

                    });

        });

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

    private String[] args(ICloudServer service) {

        File parent = (service.getTask().getTaskGroup().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        Path remoteFile = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar").toPath();
        File applicationFile = new File(folder, service.getTask().getVersion().getJar());

        IServiceTask task = service.getTask();
        int javaVersion = task.getJavaVersion();
        ServiceProcessType serviceProcessType = MainConfiguration.getInstance().getServiceProcessType();

        List<String> arguments = new ArrayList<>(Collections.singletonList("java"));

        if (javaVersion != -1) {
            MainConfiguration.getInstance().getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().ifPresent(version -> arguments.add(version.getPath()));
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


    private ITask<Boolean> downloadVersion(ServiceVersion version) {
        ITask<Boolean> task = ITask.empty();

        //checking file for version
        File file = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, version.getJar());
        file.getParentFile().mkdirs();

        //checking if file already exists or not
        if (!file.exists()) {

            //sending message
            CloudDriver.getInstance().getLogger().info("§6=> §7Requiring to download §b" + version.getJar() + "§8!");

            try {
                ProgressBar pb = new ProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK, 100L);

                //manage console
                Console console = NodeDriver.getInstance().getConsole();
                String prompt = console.getPrompt();
                console.setPrompt("");

                pb.setFakePercentage(50, 100);
                pb.setTaskName("§8» §bDownloading §f" + version.getJar());
                pb.setPrintAutomatically(true);
                pb.setExpandingAnimation(true);

                pb.setPrinter(progress -> {
                    NodeDriver.getInstance().getConsole().writePlain(ConsoleColor.toColoredString('§', progress));
                });

                URL url = new URL(version.getUrl());
                String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
                URLConnection con = url.openConnection();
                con.setRequestProperty("User-Agent", USER_AGENT);

                int contentLength = con.getContentLength();
                InputStream inputStream = con.getInputStream();

                OutputStream outputStream = Files.newOutputStream(file.toPath());
                byte[] buffer = new byte[2048];
                int length;
                int downloaded = 0;

                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    downloaded += length;
                    pb.stepTo((long) ((downloaded * 100L) / (contentLength * 1.0)));
                }
                outputStream.close();
                inputStream.close();
                pb.close("");
                task.setResult(true);
                console.setPrompt(prompt);
            } catch (Exception e) {
                task.setFailure(e);
            }
        } else {
            task.setResult(false);
        }
        return task;
    }
}
