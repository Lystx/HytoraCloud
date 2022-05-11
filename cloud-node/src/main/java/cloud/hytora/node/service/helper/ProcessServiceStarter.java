package cloud.hytora.node.service.helper;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.ConfigSplitSpacer;
import cloud.hytora.driver.common.ConfigurationFileEditor;
import cloud.hytora.driver.event.DestructiveListener;
import cloud.hytora.driver.event.defaults.server.CloudServerCacheUnregisterEvent;
import cloud.hytora.driver.event.defaults.server.CloudServerRequestScreenLeaveEvent;
import cloud.hytora.driver.services.utils.ServiceTypes;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

        this.service.getConfiguration().getVersion().download();

        // create tmp file
        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File tmpFolder = new File(parent, service.getName() + "/");

        FileUtils.forceMkdir(tmpFolder);

        // load all current group templates
        NodeDriver.getInstance().getNodeTemplateService().copyTemplates(service);

        String jar = service.getConfiguration().getVersion().getJar();
        FileUtils.copyFile(new File("storage/jars/" + jar), new File(tmpFolder, jar));

        // copy plugin
        FileUtils.copyFile(new File("storage/jars/plugin.jar"), new File(tmpFolder, "plugins/plugin.jar"));


        // TODO: 11.04.2022 change address if other node
        ServiceIdentity identity = new ServiceIdentity(service.getConfiguration().getNode(), NodeDriver.getInstance().getExecutor().getHostName(), service.getName(), NodeDriver.getInstance().getExecutor().getPort());

        // write property for identify service
        identity.save(new File(tmpFolder, "property.json"));

        // check properties and modify
        if (service.getConfiguration().getVersion().isProxy()) {
            File file = new File(tmpFolder, "config.yml");
            if (file.exists()) {
                ConfigurationFileEditor editor = new ConfigurationFileEditor(file, ConfigSplitSpacer.YAML);
                editor.setValue("host", "0.0.0.0:" + service.getPort());
                editor.saveFile();
            } else new BungeeProperties(tmpFolder, service.getPort(), service.getMaxPlayers());
        } else {
            File file = new File(tmpFolder, "server.properties");
            if (file.exists()) {
                ConfigurationFileEditor editor = new ConfigurationFileEditor(file, ConfigSplitSpacer.PROPERTIES);
                editor.setValue("server-port", String.valueOf(service.getPort()));
                editor.saveFile();
            } else new SpigotProperties(tmpFolder, service.getPort());
        }
    }

    @SneakyThrows
    public Wrapper<CloudServer> start() {
        Wrapper<CloudServer> wrapper = Wrapper.empty(CloudServer.class).denyNull();

        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
        File folder = new File(parent, service.getName() + "/");

        StartedProcess result = new ProcessExecutor()
                .command(this.args(this.service))
                .directory(folder)
                .redirectOutput(new LogOutputStream() {
                    @Override
                    protected void processLine(String line) {

                        DestructiveListener listener = CloudDriver.getInstance().getEventManager().registerSelfDestructiveHandler(ServiceOutputLineAddEvent.class, event -> {
                            String line1 = event.getLine();
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
        wrapper.setResult(this.service);

        return wrapper;
    }


    private String[] args(CloudServer service) {
        List<String> arguments = new ArrayList<>(Arrays.asList(
                "java",
                "-XX:+UseG1GC",
                "-XX:+ParallelRefProcEnabled",
                "-XX:MaxGCPauseMillis=200",
                "-XX:+UnlockExperimentalVMOptions",
                "-XX:+DisableExplicitGC",
                "-XX:+AlwaysPreTouch",
                "-XX:G1NewSizePercent=30",
                "-XX:G1MaxNewSizePercent=40",
                "-XX:G1HeapRegionSize=8M",
                "-XX:G1ReservePercent=20",
                "-XX:G1HeapWastePercent=5",
                "-XX:G1MixedGCCountTarget=4",
                "-XX:InitiatingHeapOccupancyPercent=15",
                "-XX:G1MixedGCLiveThresholdPercent=90",
                "-XX:G1RSetUpdatingPauseTimePercent=5",
                "-XX:SurvivorRatio=32",
                "-XX:+PerfDisableSharedMem",
                "-XX:MaxTenuringThreshold=1",
                "-Dusing.aikars.flags=https://mcflags.emc.gs",
                "-Daikars.new.flags=true",
                "-XX:-UseAdaptiveSizePolicy",
                "-XX:CompileThreshold=100",
                "-Dcom.mojang.eula.agree=true",
                "-Dio.netty.recycler.maxCapacity=0",
                "-Dio.netty.recycler.maxCapacity.default=0",
                "-Djline.terminal=jline.UnsupportedTerminal",
                "-Xms" + service.getConfiguration().getMemory() + "M",
                "-Xmx" + service.getConfiguration().getMemory() + "M"));

        Path remoteFile = Paths.get("storage", "jars", "remote.jar");

        File parent = (service.getConfiguration().getShutdownBehaviour().isStatic() ? NodeDriver.SERVICE_DIR_STATIC : NodeDriver.SERVICE_DIR_DYNAMIC);
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

        if (service.getConfiguration().getVersion().getServiceTypes() == ServiceTypes.SERVER) {
            arguments.add("nogui");
        }

        return arguments.toArray(new String[]{});
    }
}
