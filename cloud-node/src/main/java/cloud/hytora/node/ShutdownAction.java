package cloud.hytora.node;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.IProcessCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public enum ShutdownAction {


    KICK_PALYERS(2000L, "Disconnecting Players...", nodeDriver -> {
        PlayerExecutor.forAll().disconnect("§cThe network was shut down!");
    }),

    MODULES(1200L, "Disabling modules...", nodeDriver -> {
        nodeDriver.getModuleManager().disableModules();
        nodeDriver.getModuleManager().unregisterModules();
    }),

    WEB_SERVER(800L, "Stopping WebServer...", nodeDriver -> {
        nodeDriver.getWebServer().shutdown();
    }),

    CLOUD_SERVICES(2500L, "Stopping Services...", nodeDriver -> {

        for (ICloudService service : new ArrayList<>(nodeDriver.getServiceManager().getAllCachedServices())) {
            IProcessCloudServer cloudServer = ((IProcessCloudServer) service);
            Process process = cloudServer.getProcess();
            if (process != null) {
                process.destroyForcibly();
            }
        }
    }),

    DATABASE(1000L, "Disconnecting Database...", nodeDriver -> {
        nodeDriver.getDatabaseManager().shutdown();
    }),

    EXECUTOR(650L, "Shutting down providers...", nodeDriver -> {
        nodeDriver.getExecutor().shutdown();
        nodeDriver.getScheduledExecutor().shutdown();
    }),


    FILES(1000L, "Deleting files...", nodeDriver -> {
        FileUtils.delete(NodeDriver.SERVICE_DIR_DYNAMIC.toPath());
        FileUtils.delete(NodeDriver.STORAGE_TEMP_FOLDER.toPath());
    })

    ;




    private final long sleepTime;
    private String message;
    private final Consumer<NodeDriver> handler;
}
