package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.proxy.bungee.adapter.BungeeLocalProxyPlayer;
import cloud.hytora.bridge.proxy.bungee.events.cloud.ProxyRemoteHandler;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyEvents;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

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
        ProxyServer.getInstance().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getListeners().forEach(l -> l.getServerPriority().clear());

        new ProxyRemoteHandler();
        this.getProxy().getPluginManager().registerListener(this, new ProxyEvents());

        //update that the service is ready to use
        this.bootstrap();

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
        return this.getProxy().getPlayers().stream().map(BungeeLocalProxyPlayer::new).collect(Collectors.toList());
    }
}
