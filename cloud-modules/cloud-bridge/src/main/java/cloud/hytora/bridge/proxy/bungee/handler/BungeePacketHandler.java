package cloud.hytora.bridge.proxy.bungee.handler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.component.SimpleComponent;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

public class BungeePacketHandler implements PacketHandler<PacketCloudEntityPlayerExtension> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityPlayerExtension packet) {

        PacketBuffer buffer = packet.buffer();

        switch (buffer.readEnum(PacketCloudEntityPlayerExtension.Type.class)) {
            case BUKKIT:
                break;
            case PROXY:
                PacketCloudEntityPlayerExtension.ProxyPayLoad proxyPayLoad = buffer.readEnum(PacketCloudEntityPlayerExtension.ProxyPayLoad.class);
                UUID playerId = buffer.readUniqueId();
                ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
                if (player == null) {
                    return;
                }

                CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(playerId);
                CloudProxyPlayer proxyPlayer = cloudPlayer.asProxyPlayer();
                switch (proxyPayLoad) {
                    case PLAYER_EXECUTE_KICK:
                        proxyPlayer.disconnect(buffer.readString());
                        break;
                    case PLAYER_EXECUTE_CONNECT:
                        proxyPlayer.connect(CloudDriver.getInstance().getServiceManager().getCachedCloudService(buffer.readString()));
                        break;
                    case PLAYER_EXECUTE_MESSAGE:
                        proxyPlayer.sendMessage(buffer.readString());
                        break;
                    case PLAYER_EXECUTE_TAB_LIST:
                        proxyPlayer.setTabList(
                                buffer.readString(),
                                buffer.readString()
                        );
                        break;
                    case PLAYER_EXECUTE_COMPONENT_MESSAGE:
                        proxyPlayer.sendMessage(buffer.readObject(SimpleComponent.class));
                        break;
                }
        }
    }
}
