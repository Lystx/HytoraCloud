package cloud.hytora.driver.player.executor;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;

import java.util.UUID;
import java.util.stream.Collectors;

public interface PlayerExecutor {

    public static PlayerExecutor forAll() {
        return GlobalPlayerExecutor.INSTANCE;
    }

    public static PlayerExecutor forPlayer(ICloudPlayer player) {
        return new CloudPlayerExecutor(player);
    }

    /**
     * The uuid this executor is for
     */
    UUID getExecutorUniqueId();

    void sendMessage(String message);

    void sendMessage(Component component);

    void setTabList(String header, String footer);

    void disconnect(String reason);

    void connect(ICloudServer server);

    default void sendToFallback() {

        Task<ICloudServer> fallback = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getFallbackAsService();
        fallback.ifPresentOrElse(
                (ExceptionallyConsumer<ICloudServer>) this::connect,
                (ExceptionallyRunnable) () -> sendMessage("§cCould not find any available fallback...")
                );
    }
}
