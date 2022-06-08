package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceInfo;

import java.util.UUID;

public class GlobalPlayerExecutor implements PlayerExecutor {

    public static final GlobalPlayerExecutor INSTANCE = new GlobalPlayerExecutor();
    private final static UUID GLOBAL_UUID = new UUID(0L, 0L);

    @Override
    public UUID getExecutorUniqueId() {
        return GLOBAL_UUID;
    }

    @Override
    public void sendMessage(String message) {
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            PlayerExecutor.forPlayer(player).sendMessage(message);
        }
    }

    @Override
    public void sendMessage(ChatComponent component) {
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            PlayerExecutor.forPlayer(player).sendMessage(component);
        }
    }

    @Override
    public void disconnect(String reason) {
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            PlayerExecutor.forPlayer(player).disconnect(reason);
        }
    }

    @Override
    public void setTabList(ChatComponent header, ChatComponent footer) {
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            PlayerExecutor.forPlayer(player).setTabList(header, footer);
        }
    }

    @Override
    public void connect(ServiceInfo server) {
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            PlayerExecutor.forPlayer(player).connect(server);
        }
    }
}
