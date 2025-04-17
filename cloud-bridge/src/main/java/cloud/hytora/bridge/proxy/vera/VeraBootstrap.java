package cloud.hytora.bridge.proxy.vera;

import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.vera.adapter.VeraLocalProxyPlayer;
import cloud.hytora.bridge.proxy.vera.handler.VeraProxyRemoteHandler;
import cloud.hytora.bridge.proxy.vera.listener.VeraProxyPingListener;
import cloud.hytora.bridge.proxy.vera.listener.VeraProxyPlayerConnectionListener;
import cloud.hytora.bridge.proxy.vera.listener.VeraProxyPlayerServerListener;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.utils.*;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import de.verasoftware.proxy.api.ProxySettings;
import de.verasoftware.proxy.api.VeraProxy;
import de.verasoftware.proxy.api.commands.context.defaults.ConsoleCommandContext;
import de.verasoftware.proxy.api.event.EventManager;
import de.verasoftware.proxy.api.network.NetworkAddress;
import de.verasoftware.proxy.api.plugin.PluginState;
import de.verasoftware.proxy.api.plugin.ProxyEntityContainer;
import de.verasoftware.proxy.api.plugin.annotation.Inject;
import de.verasoftware.proxy.api.plugin.annotation.Plugin;
import de.verasoftware.proxy.api.server.ProxyServer;
import de.verasoftware.proxy.api.service.ServiceRegistry;

import java.util.Collection;
import java.util.stream.Collectors;

@Plugin(
        id = "cloud.hytora.bridge.proxy.vera.VeraBootstrap",
        displayName = "HytoraCloud-Proxy-Bridge-Vera",
        version = 1,
        authors = "Lystx"
)
public class VeraBootstrap implements PluginBridge, RemoteProxyAdapter {


    @Inject(state = PluginState.LOADED)
    public void loadServices(ServiceRegistry registry, ProxyEntityContainer container) {

        RemoteIdentity identity = getIdentity();
        if (identity.getProcessType() == ServiceProcessType.BRIDGE_PLUGIN) {
            Remote remote = new Remote(identity);
            remote.nexCacheUpdate().syncUninterruptedly().get();
        }

        Remote.getInstance().setAdapter(this);
    }

    @Inject(state = PluginState.ENABLED)
    public void enable(ProxyEntityContainer container, ServiceRegistry registry) {
        System.out.println("<=======[ VERAPROXY-BRIDGE ]=========>");
        VeraProxy.getInstance().getRegisteredServers().clear();
        
        //update that the service is ready to use
        this.bootstrap();



        registry.getProviderUnchecked(EventManager.class).registerListener(container, new VeraProxyPingListener());
        registry.getProviderUnchecked(EventManager.class).registerListener(container, new VeraProxyPlayerConnectionListener(this));
        registry.getProviderUnchecked(EventManager.class).registerListener(container, new VeraProxyPlayerServerListener());

        new VeraProxyRemoteHandler(); //initializing handler
        
        System.out.println("<=======[ VERAPROXY-BRIDGE ]=========>");
    }
    
    @Inject(state = PluginState.DISABLED)
    public void disable(ProxyEntityContainer container, ServiceRegistry registry) {

        ICloudService ICloudServer = Remote.getInstance().thisService();
        ICloudServer.setServiceState(ServiceState.STOPPING);
        ICloudServer.setReady(false);
        ICloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
        ICloudServer.update();
    }

    @Override
    public void executeCommand(String command) {
        VeraProxy.getInstance().getCommandManager().executeCommand(command, new ConsoleCommandContext(VeraProxy.getInstance()));
    }

    @Override
    public IServiceCycleData createCycleData() {
        ProxySettings settings = VeraProxy.getInstance().getServiceRegistry().getProviderUnchecked(ProxySettings.class);
        return new DefaultServiceCycleData(DocumentFactory.newJsonDocument(
                "version", VeraProxy.getInstance().getVersion(),
                "protocolVersion", VeraProxy.getInstance().getProtocolVersion(),
                "onlineCount", VeraProxy.getInstance().getOnlinePlayers().size(),
                "plugins", VeraProxy.getInstance().getPluginManager().getPlugins().stream().map(ProxyEntityContainer::getDisplayName).collect(Collectors.toList()),
                "onlineMode", settings.isPrivateMode(),
                "ipForward", settings.isIpForwarding(),
                "favicon", settings.getMotd().getFavicon(),
                "playerLimit", settings.getMaxPlayerSlots(),
                "serverCount", VeraProxy.getInstance().getRegisteredServers().size()
        ));
    }

    @Override
    public void shutdown() {

        VeraProxy.getInstance().shutdown();
    }

    @Override
    public Collection<LocalProxyPlayer> getPlayers() {
        return VeraProxy.getInstance().getOnlinePlayers().stream().map(VeraLocalProxyPlayer::new).collect(Collectors.toList());
    }

    @Override
    public void registerService(ICloudService server) {

        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        ProxyServer proxyServer = VeraProxy
                .getInstance()
                .createServer(
                        server.getName(),
                        new NetworkAddress(server.getHostName(), server.getPort()),
                        server.getMotd()
                );
        VeraProxy.getInstance().registerServer(proxyServer);
    }

    @Override
    public void unregisterService(ICloudService server) {

        if (server.getTask().getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
            return;
        }
        ProxyServer registeredServer = VeraProxy.getInstance().getRegisteredServer(server.getName());
        VeraProxy.getInstance().getRegisteredServers().remove(registeredServer);
    }

    @Override
    public void clearServices() {

        if (VeraProxy.getInstance() == null) {
            return;
        }
        VeraProxy.getInstance().getRegisteredServers().clear();
    }
}
