package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.proxy.bungee.adapter.BungeeLocalProxyPlayer;
import cloud.hytora.bridge.proxy.bungee.events.cloud.ProxyRemoteHandler;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyPlayerCommandListener;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyPingListener;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyPlayerConnectionListener;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.utils.*;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.bungee.events.server.ProxyPlayerServerListener;
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
        RemoteIdentity identity = getIdentity();
        if (identity.getProcessType() == ServiceProcessType.BRIDGE_PLUGIN) {
            Remote remote = new Remote(identity);
            remote.nexCacheUpdate().syncUninterruptedly().get();
        }

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
        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerServerListener());
        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerConnectionListener(this));
        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerCommandListener());
        this.getProxy().getPluginManager().registerListener(this, new ProxyPingListener());

        System.out.println("<=======[ BUNGEECORD ]=========>");
    }

    @Override
    public void onDisable() {
        ICloudServer ICloudServer = Remote.getInstance().thisService();
        ICloudServer.setServiceState(ServiceState.STOPPING);
        ICloudServer.setReady(false);
        ICloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
        ICloudServer.update();
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
    public IServiceCycleData createCycleData() {
        return new DefaultServiceCycleData(DocumentFactory.newJsonDocument(
                "version", ProxyServer.getInstance().getVersion(),
                "gameVersion", ProxyServer.getInstance().getGameVersion(),
                "protocolVersion", ProxyServer.getInstance().getProtocolVersion(),
                "pluginChannels", ProxyServer.getInstance().getChannels(),
                "onlineCount", ProxyServer.getInstance().getOnlineCount(),
                "plugins", ProxyServer.getInstance().getPluginManager().getPlugins().stream().map(p -> p.getDescription().getName()).collect(Collectors.toList()),
                "onlineMode", ProxyServer.getInstance().getConfig().isOnlineMode(),
                "ipForward", ProxyServer.getInstance().getConfig().isIpForward(),
                "favicon", ProxyServer.getInstance().getConfig().getFavicon(),
                "playerLimit", ProxyServer.getInstance().getConfig().getPlayerLimit(),
                "serverCount", ProxyServer.getInstance().getConfig().getServers().size()
        ));
    }

    @Override
    public Collection<LocalProxyPlayer> getPlayers() {
        return ProxyServer.getInstance().getPlayers().stream().map(BungeeLocalProxyPlayer::new).collect(Collectors.toList());
    }

    @Override
    public void registerService(ICloudServer server) {
        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        if (ProxyServer.getInstance().getServers() == null) {
            System.out.println("Couldn't access ProxyServerMap for Server " + server.getName());
            return;
        }
        ProxyServer
                .getInstance()
                .getServers()
                .put(server
                                .getName(),
                        ProxyServer
                                .getInstance()
                                .constructServerInfo(
                                        server.getName()
                                        , new InetSocketAddress(
                                                server.getHostName(), server.getPort()), server.getMotd(), false));
    }

    @Override
    public void unregisterService(ICloudServer server) {
        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        ProxyServer.getInstance().getServers().remove(server.getName());
    }

    @Override
    public void clearServices() {
        if (ProxyServer.getInstance() == null || ProxyServer.getInstance().getServers() == null) {
            return;
        }
        ProxyServer.getInstance().getServers().clear();
    }
}
