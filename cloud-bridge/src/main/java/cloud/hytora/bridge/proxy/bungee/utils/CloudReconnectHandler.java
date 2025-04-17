package cloud.hytora.bridge.proxy.bungee.utils;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.ICloudService;
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
public class CloudReconnectHandler implements ReconnectHandler {

    @Override
    public ServerInfo getServer(ProxiedPlayer player) {
        ICloudService fallback = CloudDriver
                .getInstance()
                .getServiceManager()
                .getFallbackAsServiceOrNull();
        if (fallback == null) { //what if somehow no fallback has been found? big mistake! we shouldn't allow
            player.disconnect("§cError 3825");  //players like that on the network!
            return null;

        }
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
