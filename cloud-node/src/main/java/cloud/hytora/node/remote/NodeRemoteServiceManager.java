package cloud.hytora.node.remote;

import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.common.misc.Util;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.config.IServiceCrashPrevention;
import cloud.hytora.driver.config.def.UniversalNetworkConfig;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceUnregistered;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.service.helper.CloudServerProcessWorker;
import cloud.hytora.remote.impl.RemoteServiceManager;
import org.apache.commons.io.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class NodeRemoteServiceManager extends RemoteServiceManager {


    /**
     * The worker to start service
     */
    private final CloudServerProcessWorker worker;

    public NodeRemoteServiceManager() {

        this.worker = new CloudServerProcessWorker();
    }


    @Override
    public void registerService(CloudService service) {
        super.registerService(service);

        if (!service.isRunningOn(NodeDriver.getInstance().getNode())) {
            return;
        }
        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        screenManager.registerScreen(service.getName(), false);
    }


    @Override
    public void unregisterService(CloudService service) {
        service = this.getCachedCloudService(service.getName());
        CloudDriver.getInstance().getEventManager().callEvent(new CloudEventServiceUnregistered(service.getName()), PublishingType.PROTOCOL);
        super.unregisterService(service);


        if (!service.isRunningOn(NodeDriver.getInstance().getNode())) {
            return;
        }

        ScreenManager screenManager = CloudDriver.getInstance().getProvider(ScreenManager.class);
        Screen screen = screenManager.getCachedScreen(service.getName());
        ServiceTask con = service.getTask();

        File parent = (con.getTaskGroup().getShutdownBehaviour().isStatic() ? CloudDriver.Constants.SERVICE_DIR_STATIC : CloudDriver.Constants.SERVICE_DIR_DYNAMIC);
        File folder = CloudDriver.getInstance().getServerDirectoryFormatter().supply(Tuple.of(parent, service));
        if (!service.isReady() && CloudDriver.getInstance().isRunning()) {
            NodeDriver.getInstance().getLogger().warn("§8=============§8[§cCrash§8]=============");
            NodeDriver.getInstance().getLogger().warn("§8=> §7Service§8: §8[§cName§8: §c{}, ID§8: §c{}§8]", service.getName(), service.getUniqueId());
            NodeDriver.getInstance().getLogger().warn("§8=> §7Explanation§8: §cProbably crashed during startup. Service was not authenticated by the Node");
            NodeDriver.getInstance().getLogger().warn("    §8=> §7by the time it was stopped. Probably didn't made it to the authentication-part§8.");

            File crashFolder = new File(CloudDriver.Constants.LOG_FOLDER, "crashes/");
            crashFolder.mkdirs();

            File specificCrashFolders = new File(crashFolder, con.getName() + "/");
            specificCrashFolders.mkdirs();

            File crashFile = new File(specificCrashFolders, service.getName() + "_" + UUID.randomUUID().toString() + ".log");

            try {
                cloud.hytora.common.misc.FileUtils.writeToFile(crashFile, screen.getAllCachedLines());
                NodeDriver.getInstance().getLogger().warn("§8=> §7Log saved to§8: §a{}", crashFile.getName());
                NodeDriver.getInstance().getLogger().warn("Saving logs to identify crash under {}...", crashFile.getName());
            } catch (IOException e) {
                //e.printStackTrace();
                NodeDriver.getInstance().getLogger().warn("§8=> §7Log saved to§8: §cCoudln't save §8[§e{}§8]", crashFile.getName(), e.getMessage());
            }
            IServiceCrashPrevention scp = UniversalNetworkConfig.getInstance().getServiceCrashPrevention();

            if (scp.isEnabled()) {

                NodeDriver.getInstance().getServiceQueue().addPausedGroup(con.getName());

                CloudDriver.getInstance().getScheduler().scheduleDelayedTask(() -> {
                    NodeDriver.getInstance().getServiceQueue().removePausedGroup(con.getName());
                    NodeDriver.getInstance().getServiceQueue().dequeue();
                }, scp.getUnit().toMillis(scp.getTime()));
                NodeDriver.getInstance().getLogger().warn("§8=> §7SCP§8: §aEnabled §8[§7Services of this Task wont start for §e{} {}§8]", scp.getTime(), scp.getUnit().name());
            } else {
                NodeDriver.getInstance().getLogger().warn("§8=> §7SCP§8: §cDisabled §8[§7Services will start immediately§8]");
            }
            NodeDriver.getInstance().getLogger().warn("§8=============§8[§cCrash§8]=============");
        }

        if (con.getTaskGroup().getShutdownBehaviour().isStatic()) {
            //only delete cloud files
            File property = new File(folder, "property.json");
            property.delete();

            File bridgePlugin = new File(folder, "plugins/" + CloudDriver.Constants.BRIDGE_FILE_NAME);
            bridgePlugin.delete();

            File applicationFile = new File(folder, con.getVersion().getJar());
            applicationFile.delete();

        } else {
            //dynamic -> delete everything
            if (folder.exists()) {
                try {
                    FileUtils.deleteDirectory(folder);
                } catch (IOException e) {
                }
            }
        }

        screenManager.unregisterScreen(service.getName());

        if (NodeDriver.getInstance().getNodeManager().isHeadNode()) {
            PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());
        }
    }

    @Override
    public @NotNull Task<CloudService> startService(@NotNull CloudService service) {
        if (!NodeDriver.getInstance().isReceivedFiles()) {
            Task<CloudService> task = Task.empty();
            Util.executeIf(() -> {
                worker.processService(service)
                        .onTaskSucess(task::setResult)
                        .onTaskFailed(task::setFailure);
            }, () -> NodeDriver.getInstance().isReceivedFiles());
            return task;
        }
        return worker.processService(service);
    }
}
