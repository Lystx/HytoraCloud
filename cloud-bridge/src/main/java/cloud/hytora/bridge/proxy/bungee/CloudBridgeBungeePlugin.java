package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.CloudBridge;
import cloud.hytora.bridge.proxy.bungee.handler.*;
import cloud.hytora.bridge.proxy.bungee.listener.BungeeProxyPlayerCommandListener;
import cloud.hytora.bridge.proxy.bungee.listener.BungeeProxyPingListener;
import cloud.hytora.bridge.proxy.bungee.listener.BungeePlayerConnectionListener;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.utils.*;
import cloud.hytora.bridge.IBridgePlugin;
import cloud.hytora.bridge.proxy.bungee.listener.BungeeProxyPlayerServerListener;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.IBridgeProxyExtension;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CloudBridgeBungeePlugin extends Plugin implements IBridgePlugin, IBridgeProxyExtension {

    @Override
    public void onLoad() {
        RemoteIdentity identity = getIdentity();
        if (identity.getProcessType() == ServiceProcessType.BRIDGE_PLUGIN) {
            Remote remote = new Remote(identity);
            remote.nexCacheUpdate().syncUninterruptedly().get();
        }

        CloudBridge.setRemoteExtension(this);
    }

    @Override
    public void onEnable() {
        System.out.println("<=======[ BUNGEECORD | START]=========>");

        //clearing server map (removing defaults from config)
        this.clearServices();

        //updateTask that the service is ready to use
        this.bootstrap();

        //manage values
        this.registerExecutorHandlers();
        this.setReconnectHandler();
        this.registerBungeeListeners();

        CloudBridge.init();
        CloudBridge.startUpdateTask();

        System.out.println("<=======[ BUNGEECORD | END]=========>");
    }

    @Override
    public void onDisable() {
        ICloudServer cloudServer = Remote.getInstance().thisSidesClusterParticipant();
        cloudServer.setReady(false);
        cloudServer.setServiceState(ServiceState.STOPPING);
        cloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
        cloudServer.update();
    }


    @Override
    public void shutdown() {
        this.getProxy()
                .getScheduler()
                .schedule(
                        this,
                        this.getProxy()::stop,
                        0, TimeUnit.MILLISECONDS
                );
    }

    @Override
    public IBridgeProxyExtension asProxyExtension() throws ClassCastException {
        return this;
    }

    @Override
    public void executeCommand(String command) {
        this.getProxy()
                .getPluginManager()
                .dispatchCommand(
                        this.getProxy().getConsole(),
                        command
                );
    }

    @Override
    public IServiceCycleData createCycleData() {
        return new DefaultServiceCycleData(DocumentFactory.newJsonDocument(
                "version", this.getProxy().getVersion(),
                "gameVersion", this.getProxy().getGameVersion(),
                "protocolVersion", this.getProxy().getProtocolVersion(),
                "pluginChannels", this.getProxy().getChannels(),
                "onlineCount", this.getProxy().getOnlineCount(),
                "plugins", this.getProxy().getPluginManager().getPlugins().stream().map(p -> p.getDescription().getName()).collect(Collectors.toList()),
                "onlineMode", this.getProxy().getConfig().isOnlineMode(),
                "ipForward", this.getProxy().getConfig().isIpForward(),
                "favicon", this.getProxy().getConfig().getFavicon(),
                "playerLimit", this.getProxy().getConfig().getPlayerLimit(),
                "serverCount", this.getProxy().getConfig().getServers().size()
        ));
    }


    @Override
    public void registerService(ICloudServer server) {
        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        if (this.getProxy().getServers() == null) {
            System.out.println("Couldn't access ProxyServerMap for Server " + server.getName());
            return;
        }
        getProxy()
                .getServers()
                .put(server
                                .getName(),
                        getProxy()
                                .constructServerInfo(
                                        server.getName(),
                                        new InetSocketAddress(
                                                server.getHostName(),
                                                server.getPort()
                                        ), server.getPingProperties().getMotd(), false)
                );
    }

    @Override
    public void unregisterService(ICloudServer server) {
        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        this.getProxy().getServers().remove(server.getName());
    }

    @Override
    public void clearServices() {
        this.getProxy().getServers().clear();
        this.getProxy().getConfigurationAdapter().getServers().clear();
        this.getProxy().getConfigurationAdapter().getListeners().forEach(l -> l.getServerPriority().clear());
    }

    @Override
    public void registerExecutorHandlers() {
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new BungeeCloudPlayerExecutorKickHandler());
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new BungeeCloudPlayerExecutorSendHandler());
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new BungeeCloudPlayerExecutorMessageHandler());
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new BungeeCloudPlayerExecutorComponentHandler());
        Remote.getInstance().getNetworkExecutor().registerPacketHandler(new BungeeCloudPlayerExecutorTabHandler());
    }

    /**
     * Sets the custom reconnect handler for bungeecord
     */
    private void setReconnectHandler() {
        this.getProxy().setReconnectHandler(new CloudReconnectHandler());
    }

    /**
     * Registers all bungee eventListeners
     */
    private void registerBungeeListeners() {
        this.getProxy().getPluginManager().registerListener(this, new BungeeProxyPlayerServerListener());
        this.getProxy().getPluginManager().registerListener(this, new BungeePlayerConnectionListener(this));
        this.getProxy().getPluginManager().registerListener(this, new BungeeProxyPlayerCommandListener(this));
        this.getProxy().getPluginManager().registerListener(this, new BungeeProxyPingListener());
    }

}
