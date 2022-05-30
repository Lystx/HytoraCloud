package cloud.hytora.node.service.helper;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.ConfigSplitSpacer;
import cloud.hytora.driver.common.ConfigurationFileEditor;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerRequestScreenLeaveEvent;
import cloud.hytora.driver.node.config.INodeConfig;
import cloud.hytora.driver.node.config.JavaVersion;
import cloud.hytora.driver.services.configuration.ConfigurationDownloadEntry;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.utils.WrapperEnvironment;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.node.impl.event.ServiceOutputLineAddEvent;
import cloud.hytora.node.service.NodeServiceManager;
import cloud.hytora.node.service.properties.BungeeProperties;
import cloud.hytora.node.service.properties.SpigotProperties;
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


public class ProcessServiceStarter {

    private final CloudServer service;
    private final NodeServiceManager serviceManager;



    @SneakyThrows
    public ProcessServiceStarter(NodeServiceManager serviceManager, CloudServer service) {
        this.serviceManager = serviceManager;
        this.service = service;
        this.service.setServiceState(ServiceState.STARTING);

        // add statistic to service
        NodeDriver.getInstance().getExecutor().registerStats(this.service);

        this.downloadServiceVersion(this.service.getConfiguration().getVersion());

        // create server dir
        File parent = (service.getConfiguration().getParent().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File serverDir = new File(parent, service.getName() + "/");

        FileUtils.forceMkdir(serverDir);

        // load all current configuration templates
        ServerConfiguration configuration = service.getConfiguration();

        //all templates for this service
        Collection<ServiceTemplate> templates = configuration.getParent().getTemplates(); //parent templates
        templates.addAll(configuration.getTemplates()); //configuration templates

        for (ServiceTemplate template : templates) {
            TemplateStorage storage = template.getStorage();
            if (storage != null) {
                storage.copyTemplate(service, template, serverDir);
            }
        }

        String jar = service.getConfiguration().getVersion().getJar();
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, jar), new File(serverDir, jar));

        // copy plugin
        FileUtils.copyFile(new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "plugin.jar"), new File(serverDir, "plugins/plugin.jar"));

        // TODO: 11.04.2022 change address if other node
        ServiceIdentity identity = new ServiceIdentity(service.getConfiguration().getNode(), NodeDriver.getInstance().getExecutor().getHostName(), service.getName(), NodeDriver.getInstance().getExecutor().getPort());

        // write property for identify service
        identity.save(new File(serverDir, "property.json"));

        //copy extra downloads
        for (ConfigurationDownloadEntry entry : service.getConfiguration().getParent().getDownloadEntries()) {
            CloudDriver.getInstance().getLogger().log(LogLevel.INFO, "Downloading entry for '{}' [url={}, dest={}]", service.getName(), entry.getUrl(), entry.getDestination());
            String url = entry.getUrl();
            FileUtils.copyURLToFile(new URL(url), new File(serverDir, entry.getDestination()));
        }

        // check properties and modify
        if (service.getConfiguration().getVersion().isProxy()) {
            File file = new File(serverDir, "config.yml");
            if (file.exists()) {
                ConfigurationFileEditor editor = new ConfigurationFileEditor(file, ConfigSplitSpacer.YAML);
                editor.setValue("host", "0.0.0.0:" + service.getPort());
                editor.saveFile();
            } else new BungeeProperties(serverDir, service.getPort(), service.getMaxPlayers(), Objects.requireNonNull(CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream().filter(s -> s.getConfiguration().getParent().getEnvironment() == WrapperEnvironment.MINECRAFT_SERVER).findFirst().orElse(null)));
        } else {
            File file = new File(serverDir, "server.properties");
            if (file.exists()) {
                ConfigurationFileEditor editor = new ConfigurationFileEditor(file, ConfigSplitSpacer.PROPERTIES);
                editor.setValue("server-port", String.valueOf(service.getPort()));
                editor.saveFile();
            } else new SpigotProperties(serverDir, service.getPort());
        }
    }

    @SneakyThrows
    public Wrapper<CloudServer> start() {
        Wrapper<CloudServer> wrapper = Wrapper.empty(CloudServer.class).denyNull();

        File parent = (service.getConfiguration().getParent().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
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
                                CloudDriver.getInstance().getEventManager().callEvent(new CloudServerRequestScreenLeaveEvent(CloudDriver.getInstance().getCommandManager(), CloudDriver.getInstance().getConsole(), CloudDriver.getInstance().getCommandSender(), service));
                            }
                            listener.destroy();
                        });

                        CloudDriver.getInstance().getEventManager().callEvent(new ServiceOutputLineAddEvent(service, line));
                    }
                })
                .start();

        Process process = result.getProcess();
        this.service.asCloudServer().setProcess(process);
        this.service.asCloudServer().setWorkingDirectory(folder);
        wrapper.setResult(this.service);

        return wrapper;
    }


    private String[] args(CloudServer service) {
        ServerConfiguration configuration = service.getConfiguration();
        List<String> arguments = new ArrayList<>(Arrays.asList("java"));
        int javaVersion = configuration.getJavaVersion();


        if (javaVersion != -1) {

            INodeConfig config = NodeDriver.getInstance().getConfig();
            JavaVersion version = config.getJavaVersions().stream().filter(jv -> jv.getId() == javaVersion).findFirst().orElse(null);

            if (version != null) {
                arguments.add(version.getPath()); //adding path to custom java version
            }
        }

        //adding pre defined arguments
        arguments.addAll(Arrays.asList(
                "-Dcom.mojang.eula.agree=true",
                "-Xms" + service.getConfiguration().getMemory() + "M",
                "-Xmx" + service.getConfiguration().getMemory() + "M")
        );

        //adding custom configuration arguments
        if (configuration.getParent().getJavaArguments() != null && configuration.getParent().getJavaArguments().length > 0) {
            arguments.addAll(Arrays.asList(configuration.getParent().getJavaArguments()));
        }

        Path remoteFile = new File(NodeDriver.STORAGE_VERSIONS_FOLDER, "remote.jar").toPath();

        File parent = (service.getConfiguration().getParent().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File applicationFile = new File(parent, service.getName() + "/" + service.getConfiguration().getVersion().getJar());


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

        if (service.getConfiguration().getVersion().getWrapperEnvironment() == WrapperEnvironment.MINECRAFT_SERVER) {
            arguments.add("nogui");
        }

        return arguments.toArray(new String[]{});
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
