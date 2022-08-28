package cloud.hytora.bridge.proxy.bungee.handler;


import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.packet.CloudPlayerSendServicePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCloudPlayerExecutorSendHandler implements PacketHandler<CloudPlayerSendServicePacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudPlayerSendServicePacket packet) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
        if (player == null || player.getServer().getInfo().getName().equals(packet.getService())) {
            return;
        }
        player.connect(ProxyServer.getInstance().getServerInfo(packet.getService()));
    }
}
