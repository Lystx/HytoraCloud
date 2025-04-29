package cloud.hytora.driver.player.executor;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;

import java.util.UUID;

public interface PlayerExecutor {

    public static PlayerExecutor forAll() {
        return GlobalPlayerExecutor.INSTANCE;
    }

    public static PlayerExecutor forPlayer(ICloudPlayer player) {
        return new CloudPlayerExecutor(player);
    }

    static PlayerExecutor forProxy(UUID playerId, ICloudService proxy) {
        return new PlayerIdExecutor(playerId, proxy);
    }

    /**
     * The uuid this executor is for
     */
    UUID getExecutorUniqueId();

    void sendMessage(String message);

    void sendMessage(Component component);

    void setTabList(String header, String footer);

    void disconnect(String reason);

    void connect(ICloudService server);

    default void sendToFallback() {

        Task<ICloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();
        fallback.ifPresentOrElse(
                (ExceptionallyConsumer<ICloudService>) this::connect,
                (ExceptionallyRunnable) () -> sendMessage("§cCould not find any available fallback...")
                );
    }
}
