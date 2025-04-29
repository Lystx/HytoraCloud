package cloud.hytora.remote.adapter.proxy;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.SimpleComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.remote.adapter.RemoteAdapter;

import java.util.Collection;
import java.util.UUID;

public interface RemoteProxyAdapter extends RemoteAdapter, PacketHandler<PacketCloudPlayer> {

    Collection<LocalProxyPlayer> getPlayers();

    default LocalProxyPlayer getProxyPlayer(String name) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getName().equalsIgnoreCase(name));
    }

    default LocalProxyPlayer getProxyPlayer(UUID uniqueId) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getUniqueId().equals(uniqueId));
    }

    void registerService(ICloudService server);

    void unregisterService(ICloudService server);

    void clearServices();


    void sendComponent(UUID playerId, Component component);

    @Override
    default void handle(PacketChannel wrapper, PacketCloudPlayer packet) {

        PacketBuffer buffer = packet.buffer();

        PacketCloudPlayer.PayLoad payLoad = buffer.readEnum(PacketCloudPlayer.PayLoad.class);

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

                ICloudService cachedCloudService = CloudDriver.getInstance().getServiceManager().getCachedCloudService(server);
                if (cachedCloudService == null) {
                    return;
                }
                proxyPlayer.connect(server);
                break;
        }
    }
}
