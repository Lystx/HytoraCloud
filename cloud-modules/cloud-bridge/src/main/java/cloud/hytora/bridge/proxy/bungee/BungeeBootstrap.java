package cloud.hytora.bridge.proxy.bungee;

import cloud.hytora.bridge.proxy.bungee.adapter.BungeeCloudPlayer;
import cloud.hytora.bridge.proxy.bungee.adapter.BungeePlayer;
import cloud.hytora.bridge.proxy.bungee.command.BungeeCommand;
import cloud.hytora.bridge.proxy.bungee.handler.BungeePacketHandler;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerCommandListener;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPingListener;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerConnectionListener;
import cloud.hytora.bridge.proxy.bungee.utils.CloudReconnectHandler;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.common.component.SimpleComponent;
import cloud.hytora.driver.common.component.event.ComponentEvent;
import cloud.hytora.driver.common.component.event.click.ClickEvent;
import cloud.hytora.driver.common.component.event.hover.HoverEvent;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.query.Query;
import cloud.hytora.driver.networking.query.QueryHandler;
import cloud.hytora.driver.networking.query.QueryRequest;
import cloud.hytora.driver.networking.query.QueryState;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceCycleData;
import cloud.hytora.driver.entity.services.impl.DefaultServiceCycleData;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.bridge.proxy.bungee.listener.ProxyPlayerServerListener;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.LoginCheckResult;
import cloud.hytora.remote.adapter.proxy.RemoteProxyAdapter;
import cloud.hytora.remote.adapter.proxy.LocalProxyPlayer;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class BungeeBootstrap extends Plugin implements PluginBridge, RemoteProxyAdapter {

    @Override
    public void onLoad() {

        Remote remote = Remote.init(getIdentity(), packet -> {
            if (packet == null) {
                registerCommands(CloudDriver.getInstance().getCommandManager().getCachedRegisteredCommands());
            } else {
                registerCommands(packet.getAllRegisteredCommands());
            }
        });
        remote.setAdapter(this);


        PlayerExtension extension = CloudDriver.getInstance().getProvider(PlayerExtension.class);
        CloudDriver.getInstance().setProvider(PlayerExtension.class, new PlayerExtension() {
            @Override
            public CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer) {
                return new BungeeCloudPlayer(cloudPlayer);
            }

            @Override
            public CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer) {
                return extension.createBukkitPlayer(cloudPlayer);
            }
        });
        ProxyServer.getInstance().setReconnectHandler(new CloudReconnectHandler(this));

    }

    public void registerCommands(Collection<DriverCommandInfo> commands) {

        Map<String, Collection<DriverCommandInfo>> mapping = new LinkedHashMap<>();
        for (DriverCommandInfo command : commands) {
            mapping.computeIfAbsent(command.getPath().split(" ")[0], key -> new ArrayList<>()).add(command);
        }

        for (Map.Entry<String, Command> command : ProxyServer.getInstance().getPluginManager().getCommands()) {
            if (command.getValue() instanceof BungeeCommand) {
                ProxyServer.getInstance().getPluginManager().unregisterCommand(command.getValue());
            }
        }
        CloudDriver.getInstance().getLogger().trace("Registering commands");

        mapping.forEach((name, cmds) -> {
            CloudDriver.getInstance().getLogger().trace("- Registered BungeeCommand '{}' | {}", name, cmds.stream().map(DriverCommandInfo::getPath).collect(Collectors.toList()));
            ProxyServer.getInstance().getPluginManager().registerCommand(this, new BungeeCommand(name, cmds));
        });
    }

    @Override
    public void onEnable() {
        CloudDriver.getInstance().getExecutor().registerPacketHandler(this);
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new BungeePacketHandler());
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

        Query.get().registerHandler(CloudDriver.Constants.QUERY_CHANNEL_PLAYER, new QueryHandler() {
            @Override
            public void handle(QueryRequest query) {
                PacketBuffer buffer = query.getBuffer();
                switch (query.getKey()) {
                    case CloudDriver.Constants.QUERY_KEY_PLAYER_CHECK_LOGIN:
                        UUID playerId = buffer.readUniqueId();

                        ProxiedPlayer player = ProxyServer.getInstance().getPlayer(playerId);

                        String reason = null;
                        QueryState state;
                        if (player == null) {
                            reason = "§cNo Player found on proxy!";
                            state = QueryState.FAILED;
                        } else if (player.getServer() == null || player.getServer().getInfo() == null) {
                            reason = "§cPlayer is on no server!";
                            state = QueryState.FAILED;
                        } else {
                            state = QueryState.SUCCESS;
                        }
                        String finalReason = reason;
                        query.respond()
                                .setState(state)
                                .setBuffer(buf -> {
                                    buf.writeOptionalString(finalReason);
                                });

                        break;
                    default:
                        break;
                }
            }
        });

    }

    @Override
    public void onDisable() {
        this.displayServerInfoStopping();
    }

    @Override
    public Task<Boolean> shutdown() {
        return Task.callSync(() -> {
            this.getProxy().getScheduler().schedule(this, this.getProxy()::stop, 0, TimeUnit.MILLISECONDS);

            return true;
        });
    }
    BiSupplier<CloudPlayer, LoginCheckResult> checker;

    @Override
    public void setLoginChecker(BiSupplier<CloudPlayer, LoginCheckResult> checker) {
        this.checker = checker;
    }

    @Override
    public BiSupplier<CloudPlayer, LoginCheckResult> getLoginChecker() {
        return checker;
    }

    @Override
    public void executeCommand(String command) {
        ProxyServer.getInstance().getPluginManager().dispatchCommand(ProxyServer.getInstance().getConsole(), command);
    }

    @Override
    public ServiceCycleData createCycleData() {
        return new DefaultServiceCycleData(Document.gson(
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
    public void disconnectConnection(UUID connectionId) {
    }

    @Override
    public void registerService(CloudService server) {
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
    public void unregisterService(CloudService server) {
        if (server == null || server.getTask() == null || server.getTask().getTaskGroup() == null) {
            return;
        }
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
