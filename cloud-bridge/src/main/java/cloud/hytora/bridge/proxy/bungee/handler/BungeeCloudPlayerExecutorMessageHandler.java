package cloud.hytora.bridge.proxy.bungee.handler;


import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.packet.CloudPlayerPlainMessagePacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCloudPlayerExecutorMessageHandler implements PacketHandler<CloudPlayerPlainMessagePacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudPlayerPlainMessagePacket packet) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
        if (player == null) {
            return;
        }
        player.sendMessage(packet.getMessage());
    }
}