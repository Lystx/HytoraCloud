package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.networking.packets.player.*;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.services.ServiceInfo;
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
    public void setTabList(ChatComponent header, ChatComponent footer) {
        this.sendPacketToProxy(new CloudPlayerTabListPacket(getExecutorUniqueId(), header, footer));
    }

    @Override
    public void disconnect(String reason) {
        this.sendPacketToProxy(new CloudPlayerKickPacket(getExecutorUniqueId(), this.player.getProxyServer().getName(), reason));
    }

    @Override
    public void connect(ServiceInfo server) {
        this.sendPacketToProxy(new CloudPlayerSendServicePacket(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(Packet packet) {
        ServiceInfo proxyServer = player.getProxyServer();
        if (proxyServer != null) {
            proxyServer.sendPacket(packet);
        }
    }
}
