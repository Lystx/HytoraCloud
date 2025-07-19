package cloud.hytora.remote.adapter.proxy;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.common.component.SimpleComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.remote.adapter.RemoteAdapter;

import java.util.Collection;
import java.util.UUID;

public interface RemoteProxyAdapter extends RemoteAdapter, PacketHandler<PacketCloudEntityPlayer> {

    Collection<LocalProxyPlayer> getPlayers();

    default LocalProxyPlayer getProxyPlayer(String name) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getName().equalsIgnoreCase(name));
    }

    default LocalProxyPlayer getProxyPlayer(UUID uniqueId) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getUniqueId().equals(uniqueId));
    }

    void registerService(CloudService server);

    void unregisterService(CloudService server);

    void clearServices();


    void sendComponent(UUID playerId, Component component);

    @Override
    default void handle(PacketChannel channel, PacketCloudEntityPlayer packet) {

        PacketBuffer buffer = packet.buffer();

        PacketCloudEntityPlayer.PayLoad payLoad = packet.getPayLoad();

        if (!payLoad.name().toLowerCase().contains("execute")) {
            return;
        }

        UUID playerId = buffer.readUniqueId();
        LocalProxyPlayer proxyPlayer = getProxyPlayer(playerId);

        switch (payLoad) {

            case PLAYER_EXECUTE_KICK:
                String kickReason = buffer.readOptionalString();
                if (kickReason == null) {
                    kickReason = "No reason provided!";
                }
                proxyPlayer.disconnect(kickReason);
                break;
            case PLAYER_EXECUTE_MESSAGE:
                String message = buffer.readOptionalString();
                if (message == null) {
                    return;
                }
                proxyPlayer.sendMessage(message);
                break;

            case PLAYER_EXECUTE_COMPONENT_MESSAGE:
                Component component = buffer.readObject(SimpleComponent.class);
                sendComponent(playerId, component);
                break;
            case PLAYER_EXECUTE_TAB_LIST:
                String header = buffer.readString();
                String footer = buffer.readString();

                proxyPlayer.setTabList(header, footer);
                break;
            case PLAYER_EXECUTE_CONNECT:
                String server = buffer.readString();

                CloudService cachedCloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server);
                if (cachedCloudService == null) {
                    return;
                }
                proxyPlayer.connect(server);
                break;
        }
    }
}
