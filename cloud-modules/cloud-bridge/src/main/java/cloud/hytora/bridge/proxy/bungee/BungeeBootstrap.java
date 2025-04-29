package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.proxy.bungee.adapter.BungeePlayer;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerCommandListener;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPingListener;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerConnectionListener;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.SimpleComponent;
import cloud.hytora.driver.component.event.ComponentEvent;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverEvent;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.utils.*;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerServerListener;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class BungeeBootstrap extends Plugin implements PluginBridge, RemoteProxyAdapter {

    @Override
    public void onLoad() {

        Remote remote = Remote.init(getIdentity());
        remote.setAdapter(this);

        ProxyServer.getInstance().setReconnectHandler(new CloudReconnectHandler(this));
    }

    @Override
    public void onEnable() {
        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
        this.initialize();
        System.out.println("<=======[ BUNGEECORD ]=========>");
        ProxyServer.getInstance().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getServers().clear();
        ProxyServer.getInstance().getConfigurationAdapter().getListeners().forEach(l -> l.getServerPriority().clear());

        //update that the service is ready to use
        this.updateServiceInfo();

        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerServerListener());
        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerConnectionListener(this));
        this.getProxy().getPluginManager().registerListener(this, new ProxyPlayerCommandListener());
        this.getProxy().getPluginManager().registerListener(this, new ProxyPingListener());

        System.out.println("<=======[ BUNGEECORD ]=========>");
    }

    @Override
    public void onDisable() {
        this.displayServerInfoStopping();
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
        return ProxyServer.getInstance().getPlayers().stream().map(BungeePlayer::new).collect(Collectors.toList());
    }

    @Override
    public void registerService(ICloudService server) {
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
    public void unregisterService(ICloudService server) {
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

    /**
     * Creates a {@link TextComponent} from a {@link SimpleComponent}
     *
     * @param chatComponent the cloudComponent
     * @return built md5 textComponent
     */
    private TextComponent createTextComponentFromCloudRecursive(SimpleComponent chatComponent) {
        TextComponent textComponent = new TextComponent(chatComponent.getContent());
        ComponentEvent<ClickEvent> clickEvent = chatComponent.getClickEvent();
        if (clickEvent != null) {
            textComponent.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(net.md_5.bungee.api.chat.ClickEvent.Action.valueOf(clickEvent.getType().name()), clickEvent.getValue()));
        }
        ComponentEvent<HoverEvent> hoverEvent = chatComponent.getHoverEvent();
        if (hoverEvent != null) {
            textComponent.setHoverEvent(new net.md_5.bungee.api.chat.HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.valueOf(hoverEvent.getType().name()), new BaseComponent[]{new TextComponent(hoverEvent.getValue())}));
        }
        for (Component cloudComponent : chatComponent.getSubComponents()) {
            textComponent.addExtra(createTextComponentFromCloudRecursive((SimpleComponent) cloudComponent));
        }

        textComponent.setBold(chatComponent.isBold());
        textComponent.setItalic(chatComponent.isItalic());
        textComponent.setStrikethrough(chatComponent.isStrikeThrough());
        textComponent.setObfuscated(chatComponent.isObfuscated());
        textComponent.setUnderlined(chatComponent.isUnderlined());

        return textComponent;
    }



    @Override
    public void sendComponent(UUID playerId, Component component) {
        SimpleComponent sp = (SimpleComponent)component;
        TextComponent textComponentFromCloudRecursive = createTextComponentFromCloudRecursive(sp);

        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);
        if (player != null) {
            player.sendMessage(textComponentFromCloudRecursive);
        }
    }
}
