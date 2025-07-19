package cloud.hytora.bridge.proxy.bungee.adapter;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.common.component.style.ComponentColor;
import cloud.hytora.driver.common.exception.HytoraCloudException;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.function.Consumer;

@AllArgsConstructor
public class BungeeCloudPlayer implements CloudProxyPlayer {

    private final CloudPlayer cloudPlayer;

    @Override
    public void sendMessage(String message) {
        player(proxiedPlayer -> {
            proxiedPlayer.sendMessage(ComponentColor.translateAlternateColorCodes('&', message));
        });
    }

    @Override
    public void sendMessage(Component component) {
        RemoteProxyAdapter proxyAdapter = Remote.getInstance().getProxyAdapterOrNull();
        if (proxyAdapter == null) {
            throw new HytoraCloudException("ProxyAdapter may not be null on BungeeCord instance. Contact administrator!");
        }
        proxyAdapter.sendComponent(this.cloudPlayer.getUniqueId(), component);
    }

    @Override
    public void setTabList(String header, String footer) {
        player(proxiedPlayer -> {
            proxiedPlayer.setTabHeader(
                    new TextComponent(header),
                    new TextComponent(footer)
            );
        });
    }

    @Override
    public void disconnect(String reason) {
        player(player -> {
            player.disconnect(ComponentColor.translateAlternateColorCodes('&', reason));
        });
    }

    @Override
    public void connect(CloudService server) {
        player(player -> {
            player.connect(ProxyServer.getInstance().getServerInfo(server.getName()));
        });
    }


    <T> T get(BiSupplier<ProxiedPlayer, T> supply, T defValue) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.cloudPlayer.getUniqueId());
        if (player == null) {
            return defValue;
        }
        return supply.supply(player);
    }

    void player(Consumer<ProxiedPlayer> handler) {
        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(this.cloudPlayer.getUniqueId());
        if (player == null) {
            return;
        }
        handler.accept(player);
    }
}
