package cloud.hytora.node.impl.player.extension;

import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;

import java.util.function.Consumer;

@AllArgsConstructor
public class NodeProxyPlayer implements CloudProxyPlayer {

    private final CloudPlayer cloudPlayer;

    @Override
    public void sendMessage(String message) {
        perform(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_MESSAGE, buf -> buf.writeString(message));
    }

    @Override
    public void sendMessage(Component component) {
        perform(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_COMPONENT_MESSAGE, buf -> buf.writeObject(component));
    }

    @Override
    public void setTabList(String header, String footer) {
        perform(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_TAB_LIST, buf -> buf.writeString(header).writeString(footer));
    }

    @Override
    public void disconnect(String reason) {
        perform(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_KICK, buf -> buf.writeString(reason));
    }

    @Override
    public void connect(CloudService server) {
        perform(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_CONNECT, buf -> buf.writeString(server.getName()));
    }

    void perform(PacketCloudEntityPlayerExtension.ProxyPayLoad payLoad, Consumer<PacketBuffer> buffer) {
        PacketCloudEntityPlayerExtension packetCloudEntityPlayerExtension = PacketCloudEntityPlayerExtension.forProxy(payLoad, buf -> buf.writeUniqueId(this.cloudPlayer.getUniqueId()).append(buffer));

        this.cloudPlayer.getProxyServer().sendPacket(packetCloudEntityPlayerExtension);
    }
}
