package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceInfo;

import java.util.UUID;

public interface PlayerExecutor {

    public static PlayerExecutor forAll() {
        return GlobalPlayerExecutor.INSTANCE;
    }

    public static PlayerExecutor forPlayer(CloudPlayer player) {
        return new CloudPlayerExecutor(player);
    }

    /**
     * The uuid this executor is for
     */
    UUID getExecutorUniqueId();

    void sendMessage(String message);

    void sendMessage(ChatComponent component);

    void setTabList(ChatComponent header, ChatComponent footer);

    void disconnect(String reason);

    void connect(ServiceInfo server);

}
