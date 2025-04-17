package cloud.hytora.modules.smartproxy;

import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.document.Document;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.modules.smartproxy.commands.SmartProxyCommand;
import cloud.hytora.modules.smartproxy.packet.MinecraftPacket;
import cloud.hytora.modules.smartproxy.packet.PingPacket;
import cloud.hytora.modules.smartproxy.proxy.ForwardDownStream;
import cloud.hytora.modules.smartproxy.proxy.ForwardUpStream;
import cloud.hytora.modules.smartproxy.server.ProxyNettyServer;
import cloud.hytora.modules.smartproxy.utils.MinecraftState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import lombok.Getter;
import lombok.Setter;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.stream.Collectors;

@ModuleConfiguration(
        name = "module-smartProxy",
        main = SmartProxyModule.class,
        author = "unknown",
        description = "",
        version = "SNAPSHOT-1.0",
        website = "",
        copyType = ModuleCopyType.NONE,
        environment = ModuleEnvironment.NODE
)
@Getter
@Setter
public class SmartProxyModule extends AbstractModule {


    //Config stuff
    /**
     * The searching mode for a free proxy
     */
    private String proxySearchMode;

    /**
     * If module is enabled
     */
    private boolean enabled;

    /**
     * All registered {@link MinecraftPacket}s
     */
    public static final Map<Integer, Class<? extends MinecraftPacket>> MINECRAFT_PACKETS = new HashMap<>();

    /**
     * The connection states
     */
    public final static AttributeKey<MinecraftState> CONNECTION_STATE = AttributeKey.valueOf("connectionstate");

    /**
     * The {@link ForwardDownStream}s
     */
    public final static AttributeKey<ForwardDownStream> FORWARDING_DOWN = AttributeKey.valueOf("downstreamhandler");

    /**
     * The {@link ForwardUpStream}s
     */
    public final static AttributeKey<ForwardUpStream> FORWARDING_UP = AttributeKey.valueOf("upstreamhandler");

    /**
     * The netty server
     */
    private ProxyNettyServer proxyNettyServer;

    /**
     * The netty channel
     */
    private Channel channel;

    /**
     * The netty worker group
     */
    private EventLoopGroup workerGroup;

    /**
     * The static instance
     */
    @Getter
    private static SmartProxyModule instance;

    public SmartProxyModule(ModuleController controller) {
        super(controller);
    }
    
    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void loadConfig() {
        instance = this;

        StorableDocument config = this.getController().getConfig();

        if (config.isEmpty()) {
            config.set("enabled", true);
            config.set("proxySearchMode", "RANDOM");
            config.save();
        }

        this.enabled = this.getController().getConfig().getBoolean("enabled", true);
        this.proxySearchMode = this.getController().getConfig().getString("proxySearchMode", "RANDOM");

        INetworkConfig networkConfig = CloudDriver.getInstance().getNetworkConfig();
        if (enabled) {
            if (networkConfig.getProxyStartPort() == 25565) {
                CloudDriver.getInstance().getLogger().info("§7Default-Proxy-Port was §b25565 §7had to change to §325566 §7in order to make §bSmartProxy §7work§h!");
                networkConfig.setProxyStartPort(25566);
            }

            MINECRAFT_PACKETS.put(0x00, PingPacket.class);
        } else {
            if (networkConfig.getProxyStartPort() != 25565) {
                CloudDriver.getInstance().getLogger().info("SmartProxy-System is currently §cdisabled§h! §7Setting Default-Proxy-Port §7back to §b25565§h!");
                networkConfig.setProxyStartPort(25565);
            }
        }

        networkConfig.update();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void startModule() {
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {

            if (this.enabled) {
                this.workerGroup = new NioEventLoopGroup();
                this.proxyNettyServer = new ProxyNettyServer("127.0.0.1", 25565);
                CloudDriver.getInstance().getCommandManager().registerCommand(new SmartProxyCommand());
                try {
                    proxyNettyServer.bind();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 20L);
    }

    @ModuleTask(id = 3, state = ModuleState.DISABLED)
    public void stopModule() {
        this.proxyNettyServer.unbind();
    }

    /**
     * Forwards a ping request to a {@link cloud.hytora.driver.services.ICloudService} and connects the requester to it
     *
     * @param state the login state
     * @param channel the netty channel
     * @param proxy the proxy
     * @param login the login packet as buf
     */
    public void forwardRequestToNextProxy(Channel channel, ICloudService proxy, ByteBuf login, int state) {
        ForwardDownStream downstreamHandler = channel.attr(FORWARDING_DOWN).get() == null ? new ForwardDownStream(channel) : channel.attr(FORWARDING_DOWN).get();
        channel.attr(FORWARDING_DOWN).set(downstreamHandler);
        channel.attr(CONNECTION_STATE).set(MinecraftState.HANDSHAKE);

        new Bootstrap()
                .group(this.workerGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
                .handler(new ChannelInitializer<Channel>() {
                    @Override
                    public void initChannel(Channel channel) throws Exception {
                        channel.pipeline().addLast(downstreamHandler);
                    }
                }).connect(new InetSocketAddress(proxy.getHostName(), proxy.getPort())).addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {

                        if (channel.attr(FORWARDING_UP).get() == null) {
                            ForwardUpStream upstreamHandler = new ForwardUpStream(channelFuture.channel(), downstreamHandler);
                            channel.pipeline().addLast(upstreamHandler);
                            channel.attr(FORWARDING_UP).set(upstreamHandler);
                        } else {
                            channel.attr(FORWARDING_UP).get().setChannel(channelFuture.channel());
                        }

                        if (channel.pipeline().get("minecraftdecoder") != null) {
                            channel.pipeline().remove("minecraftdecoder");
                        }

                        CloudDriver.getInstance()
                                .getChannelMessenger()
                                        .sendChannelMessage(
                                                ChannelMessage
                                                        .builder()
                                                        .channel("cloud_module_smartproxy")
                                                        .key("PROXY_SET_IP")
                                                        .receivers(proxy)
                                                        .document(
                                                                Document.newJsonDocument()
                                                                        .set("CLIENT_ADDRESS", channel.remoteAddress().toString())
                                                                        .set("CHANNEL_ADDRESS", channelFuture.channel().localAddress().toString())
                                                        )
                                                        .build()
                                        );
                        login.resetReaderIndex();
                        channelFuture.channel().writeAndFlush(login.retain());
                        channel.attr(CONNECTION_STATE).set(MinecraftState.PROXY);
                    } else {
                        channel.close();
                        channelFuture.channel().close();
                    }
                });
    }

    /**
     * The last provided random service
     */
    private ICloudService lastRandom;

    /**
     * Tries to find the best free proxy which is not already full
     * depending on your search mode you provided in the config
     *
     * 'RANDOM' will just search a random proxy
     * 'BALANCED' will try to balance all proxies
     * 'FILL' will try to fill all proxies
     *
     * @param group the group youre trying to get proxies of
     * @return service if found or null
     */
    public ICloudService getFreeProxy(IServiceTask group, int state) {
        if (group.getOnlineServices().size() == 1) {
            return group.getOnlineServices().get(0);
        }
        //Only free proxies
        ICloudService value = null;
        List<ICloudService> proxies = group.getOnlineServices()
                .stream().
                filter(proxy -> proxy.getOnlinePlayers().size() < proxy.getTask().getDefaultMaxPlayers())
                .filter(proxy -> proxy.getServiceVisibility() == ServiceVisibility.VISIBLE)
                .collect(Collectors.toList());
        if (!proxies.isEmpty()) {
            if (this.proxySearchMode.equalsIgnoreCase("RANDOM")) {
                value = proxies.get(new Random().nextInt(proxies.size()));
            } else {
                proxies.sort(Comparator.comparing(service -> service.getOnlinePlayers().size()));
                if (this.proxySearchMode.equalsIgnoreCase("BALANCED")) {
                    value = proxies.get(0);
                } else {
                    for (int i = proxies.size() - 1; i >= 0; i--) {
                        ICloudService server = proxies.get(i);
                        if (server.getOnlinePlayers().size() < server.getTask().getDefaultMaxPlayers()) {
                            value = server;
                            break;
                        }
                    }
                }
            }
        }
        if (value != null) {
            if (lastRandom != null) {
                if (lastRandom.getName().equalsIgnoreCase(value.getName())) {
                    return getFreeProxy(group, state);
                } else {
                    if (state == 221) {
                        lastRandom = value;
                    }
                }
            } else {
                if (state == 221) {
                    lastRandom = value;
                }
            }
        }
        if (state == 349) {
            return lastRandom;
        }
        return value;
    }
}
