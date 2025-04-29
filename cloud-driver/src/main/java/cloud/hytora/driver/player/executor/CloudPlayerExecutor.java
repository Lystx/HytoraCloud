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
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerPlainMessage(getExecutorUniqueId(), message));
    }

    @Override
    public void sendMessage(Component component) {
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerComponentMessage(getExecutorUniqueId(), component));
    }

    @Override
    public void setTabList(String header, String footer) {
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerTabList(getExecutorUniqueId(), header, footer));
    }

    @Override
    public void disconnect(String reason) {
        PacketCloudPlayer packet = PacketCloudPlayer.forPlayerKick(getExecutorUniqueId(), reason);
        this.sendPacketToProxy(packet);
    }

    @Override
    public void connect(ICloudService server) {
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerSend(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(AbstractPacket packet) {
        ICloudService proxyServer = player.getProxyServer();
        if (proxyServer != null) {
            proxyServer.sendPacket(packet);
        }
    }
}
