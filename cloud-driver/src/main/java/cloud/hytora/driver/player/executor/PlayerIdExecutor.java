package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class PlayerIdExecutor implements PlayerExecutor {

    private final UUID executorUniqueId;
    private final ICloudService proxyServer;

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
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerKick(getExecutorUniqueId(), reason));
    }

    @Override
    public void connect(ICloudService server) {
        this.sendPacketToProxy(PacketCloudPlayer.forPlayerSend(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(AbstractPacket packet) {
        if (proxyServer != null) {
            proxyServer.sendPacket(packet);
        }
    }
}
