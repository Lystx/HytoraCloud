package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.proxy.bungee.adapter.BungeeLocalProxyPlayer;
import cloud.hytora.bridge.proxy.bungee.events.cloud.ProxyRemoteHandler;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyEvents;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BungeeBootstrap extends Plugin implements PluginBridge, RemoteProxyAdapter {

    @Override
    public void onLoad() {
        ProxyServer.getInstance().setReconnectHandler(new CloudReconnectHandler());
        Remote.getInstance().setAdapter(this);
    }

    @Override
    public void onEnable() {
        System.out.println("<=======[ BUNGEECORD ]=========>");
        ProxyServer.getInstance().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getListeners().forEach(l -> l.getServerPriority().clear());

        //update that the service is ready to use
        this.bootstrap();

        new ProxyRemoteHandler();
        this.getProxy().getPluginManager().registerListener(this, new ProxyEvents());

        System.out.println("<=======[ BUNGEECORD ]=========>");
    }

    @Override
    public void onDisable() {
        Remote.getInstance().thisService().edit(service -> {
            service.setServiceState(ServiceState.STOPPING);
            service.setServiceVisibility(ServiceVisibility.INVISIBLE);
        });
    }

    @Override
    public void shutdown() {
        this.getProxy().getScheduler().schedule(this, this.getProxy()::stop, 0, TimeUnit.MILLISECONDS);
    }


    @Override
    public void executeCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }

    @Override
    public Collection<LocalProxyPlayer> getPlayers() {
        return ProxyServer.getInstance().getPlayers().stream().map(BungeeLocalProxyPlayer::new).collect(Collectors.toList());
    }

    @Override
    public void registerService(CloudServer server) {
        if (server.getConfiguration().getParent().getEnvironment() == SpecificDriverEnvironment.PROXY_SERVER) {
            return;
        }
        ProxyServer.getInstance().getServers().put(server.getName(), ProxyServer.getInstance().constructServerInfo(server.getName(), new InetSocketAddress(server.getHostName(), server.getPort()), server.getMotd(), false));
    }

    @Override
    public void unregisterService(CloudServer server) {
        if (server.getConfiguration().getParent().getEnvironment() == SpecificDriverEnvironment.PROXY_SERVER) {
            return;
        }
        ProxyServer.getInstance().getServers().remove(server.getName());
    }

    @Override
    public void clearServices() {
        ProxyServer.getInstance().getServers().clear();
    }
}
