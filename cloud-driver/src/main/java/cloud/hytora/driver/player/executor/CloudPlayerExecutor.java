package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.networking.packets.player.CloudPlayerComponentMessagePacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerKickPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerPlainMessagePacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerSendServicePacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.CloudServer;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CloudPlayerExecutor implements PlayerExecutor {

    private final CloudPlayer player;

    @Override
    public UUID getExecutorUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        this.sendPacketToProxy(new CloudPlayerPlainMessagePacket(getExecutorUniqueId(), message));
    }

    @Override
    public void sendMessage(ChatComponent component) {
        this.sendPacketToProxy(new CloudPlayerComponentMessagePacket(getExecutorUniqueId(), component));
    }

    @Override
    public void disconnect(String reason) {
        this.sendPacketToProxy(new CloudPlayerKickPacket(getExecutorUniqueId(), this.player.getProxyServer().getName(), reason));
    }

    @Override
    public void connect(CloudServer server) {
        this.sendPacketToProxy(new CloudPlayerSendServicePacket(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(Packet packet) {
        CloudServer proxyServer = player.getProxyServer();
        if (proxyServer != null) {
            proxyServer.sendPacket(packet);
        }
    }
}
