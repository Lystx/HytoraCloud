package cloud.hytora.driver.player.executor;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.*;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor @Getter
public class UnknownExecutor implements PlayerExecutor {

    private final UUID executorUniqueId;

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
        this.sendPacketToProxy(new CloudPlayerKickPacket(getExecutorUniqueId(), "UNKNOWN", reason));
    }

    @Override
    public void connect(ICloudServer server) {
        this.sendPacketToProxy(new CloudPlayerSendServicePacket(getExecutorUniqueId(), server.getName()));
    }

    private void sendPacketToProxy(AbstractPacket packet) {
        //find first proxy as the player couldn't find any proxy
        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY)
                .stream()
                .findFirst()
                .ifPresent(
                        proxyServer ->
                                proxyServer.sendPacket(packet)
                );
    }
}
