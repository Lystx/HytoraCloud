package cloud.hytora.driver.entity.player.extension;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.entity.services.CloudService;

public interface CloudProxyPlayer {

    void sendMessage(String message);

    void sendMessage(Component component);

    void setTabList(String header, String footer);

    void disconnect(String reason);

    void connect(CloudService server);


    default void sendToFallback() {

        Task<CloudService> fallback = CloudDriver.getInstance().getServiceManager().getFallbackAsService();
        fallback.ifPresentOrElse(
                (ExceptionallyConsumer<CloudService>) this::connect,
                (ExceptionallyRunnable) () -> sendMessage("§cCould not find any available fallback...")
        );
    }
}
