package cloud.hytora.bridge.proxy.bungee.handler;


import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.packet.CloudPlayerTabListPacket;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class BungeeCloudPlayerExecutorTabHandler implements PacketHandler<CloudPlayerTabListPacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudPlayerTabListPacket packet) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(packet.getUuid());
        if (player == null) {
            return;
        }

        String header = packet.getHeader();
        String footer = packet.getFooter();

        player.setTabHeader(
                new TextComponent(header),
                new TextComponent(footer)
        );
    }
}
