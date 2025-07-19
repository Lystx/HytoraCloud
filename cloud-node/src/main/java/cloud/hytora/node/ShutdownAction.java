package cloud.hytora.node;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.def.UniversalCloudMessages;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.NodeSpecificCloudService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public enum ShutdownAction {


    KICK_PALYERS(1000L, "node.shutdown.message.players", nodeDriver -> {
        UniversalCloudMessages messages = CloudDriver.getInstance().getConfigManager().getConfig().getMessages();
        if (messages == null) {

            CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().forEach(cloudPlayer -> cloudPlayer.asProxyPlayer().disconnect("Node shutdown"));
            return;
        }
        CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers().forEach(cloudPlayer -> cloudPlayer.asProxyPlayer().disconnect(messages.getPrefix() +messages.getNetworkShutdown()));
    }),

    MODULES(600L, "node.shutdown.message.modules", nodeDriver -> {
        nodeDriver.getModuleManager().disableModules();
        nodeDriver.getModuleManager().unregisterModules();
    }),

    WEB_SERVER(200L, "node.shutdown.message.web", nodeDriver -> {
        if (nodeDriver.getWebServer() != null) {
            nodeDriver.getWebServer().shutdown();
        }
    }),

    CLOUD_SERVICES(3000L, "node.shutdown.message.services", nodeDriver -> {

        for (CloudService service : new ArrayList<>(nodeDriver.getServiceManager().getAllCachedServices())) {
            if (!service.isRunningOn(nodeDriver.getNode())) { //does not run on this node... ignoring
                continue;
            }
            NodeSpecificCloudService cloudServer = ((NodeSpecificCloudService) service);
            Process process = cloudServer.getProcess();
            if (process != null) {
                process.destroyForcibly();
            } else {
                nodeDriver.getLogger().warn("Could not terminate {} because Process was provided as null.", service.getName());
            }
        }
    }),

    DATABASE(250L, "node.shutdown.message.database", nodeDriver -> {
        nodeDriver.getDatabaseManager().shutdown();
    }),

    EXECUTOR(100L, "node.shutdown.message.providers", nodeDriver -> {
        nodeDriver.getScheduledExecutor().shutdown();
        nodeDriver.getExecutor().shutdown();
    }),


    FILES(350L, "node.shutdown.message.files", nodeDriver -> {
        FileUtils.delete(CloudDriver.Constants.SERVICE_DIR_DYNAMIC.toPath());
        FileUtils.delete(CloudDriver.Constants.STORAGE_TEMP_FOLDER.toPath());

        if (nodeDriver.getConfigManager().isRemote()) {
            FileUtils.deleteFile(CloudDriver.Constants.MODULE_FOLDER.toPath());
            try {org.apache.commons.io.FileUtils.deleteDirectory(CloudDriver.Constants.STORAGE_VERSIONS_FOLDER);} catch (IOException ignored) {}
        }
    })

    ;




    private final long sleepTime;
    private final String message;
    private final Consumer<NodeDriver> handler;
}
