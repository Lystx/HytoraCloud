package cloud.hytora.bridge.proxy.bungee.utils;

import cloud.hytora.bridge.proxy.bungee.BungeeBootstrap;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudService;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ReconnectHandler;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * This is the reconnectHandler for BungeeCord-API
 *
 * We only need the Method {@link #getServer(ProxiedPlayer)}
 * This method defines the Server a player will get direct to when logging in.
 *
 *
 * @author Lystx
 */
@AllArgsConstructor
public class CloudReconnectHandler implements ReconnectHandler {

    private final BungeeBootstrap bungeeBootstrap;


    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        ICloudService fallback = CloudDriver
                .getInstance()
                .getServiceManager()
                .getFallbackAsService().orElse(null);
        if (fallback == null) { //what if somehow no fallback has been found? big mistake! we shouldn't allow
            player.disconnect("§cError 3825");  //players like that on the network!
            return null;

        }
        bungeeBootstrap.setFirstJoinServer(player.getUniqueId(), fallback);

        return ProxyServer.getInstance().getServerInfo(fallback.getName());
    }

    @Override
    public void setServer(ProxiedPlayer player) {
        //not needed
    }

    @Override
    public void save() {
        //not needed
    }

    @Override
    public void close() {
        //not needed
    }

}
