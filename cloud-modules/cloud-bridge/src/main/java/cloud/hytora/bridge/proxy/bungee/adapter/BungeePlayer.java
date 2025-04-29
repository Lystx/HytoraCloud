package cloud.hytora.bridge.proxy.bungee.adapter;

import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.UUID;

@AllArgsConstructor
public class BungeePlayer implements LocalProxyPlayer {

    private final ProxiedPlayer player;


    @Override
    public void setTabList(String header, String footer) {
        this.player.setTabHeader(
                new TextComponent(header),
                new TextComponent(footer)
        );
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueId() {
        return player.getUniqueId();
    }

    @Override
    public void disconnect(String reason) {
        player.disconnect(reason);
    }

    @Override
    public void sendMessage(String message) {
        player.sendMessage(message);
    }

    @Override
    public String getServer() {
        return player.getServer().getInfo().getName();
    }

    @Override
    public void connect(String server) {
        player.connect(ProxyServer.getInstance().getServerInfo(server));
    }
}
