package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;

import java.util.UUID;

@AllArgsConstructor
public class CloudPlayerExecutor implements PlayerExecutor {

    private final ICloudPlayer player;

    @Override
    public UUID getExecutorUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void sendMessage(String message) {
        this.sendPacketToProxy(new CloudPlayerPlainMessagePacket(getExecutorUniqueId(), message));
    }

    @Override
    public void sendMessage(Component component) {
        this.sendPacketToProxy(new CloudPlayerComponentMessagePacket(getExecutorUniqueId(), component));
    }

    @Override
    public void setTabList(String header, String footer) {
        this.sendPacketToProxy(new CloudPlayerTabListPacket(getExecutorUniqueId(), header, footer));
    }

    @Override
    public void disconnect(String reason) {
        this.sendPacketToProxy(new CloudPlayerKickPacket(getExecutorUniqueId(), this.player.getProxyServer() == null ? "UNKNOWN" : this.player.getProxyServer().getName(), reason));
    }

    @Override
    public void connect(ICloudService server) {
        this.sendPacketToProxy(new CloudPlayerSendServicePacket(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(AbstractPacket packet) {
        ICloudService proxyServer = player.getProxyServer();
        if (proxyServer != null) {
            proxyServer.sendPacket(packet);
        }
    }
}
