package cloud.hytora.http.impl;

import cloud.hytora.common.collection.pair.Pair;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


public class NettyHttpServer implements HttpServer {

    protected final EventLoopGroup bossEventLoopGroup = NettyUtils.newEventLoopGroup();
    protected final EventLoopGroup workerEventLoopGroup = NettyUtils.newEventLoopGroup();

    protected final HttpHandlerRegistry handlerRegistry = new HttpHandlerRegistry();
    protected final HttpAuthRegistry authRegistry = new HttpAuthRegistry();

    protected final Collection<WebSocketChannel> websocketChannels = new CopyOnWriteArrayList<>();

    @Override
    public HttpServer addListener(@Nonnull HttpAddress address) {

        try {
            new ServerBootstrap()
                    .group(bossEventLoopGroup, workerEventLoopGroup)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childOption(ChannelOption.IP_TOS, 24)
                    .childOption(ChannelOption.AUTO_READ, true)
                    .childOption(ChannelOption.ALLOCATOR, ByteBufAllocator.DEFAULT)
                    .channel(NettyUtils.getServerChannel())
                    //.channelFactory(NettyUtils.getServerChannelFactory())
                    .childHandler(new NettyHttpServerInitializer(this, address))
                    .bind(address.getHost(), address.getPort())
                    .addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE)
                    .addListener(ChannelFutureListener.CLOSE_ON_FAILURE)
                    .sync()
                    .channel();

        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        return this;
    }

    @Override
    public void shutdown() {
        try {
            bossEventLoopGroup.shutdownGracefully();
            workerEventLoopGroup.shutdownGracefully();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Nonnull
    @Override
    public Collection<WebSocketChannel> getWebSocketChannels() {
        return websocketChannels;
    }

    @Override
    public void sendWebSocketFrame(@Nonnull WebSocketFrameType type, @Nonnull byte[] data) {
        for (WebSocketChannel channel : websocketChannels) {
            channel.sendFrame(type, data);
        }
    }

    @Override
    public void sendWebSocketFrame(@Nonnull WebSocketFrameType type, @Nonnull String text) {
        for (WebSocketChannel channel : websocketChannels) {
            channel.sendFrame(type, text);
        }
    }

    @Nonnull
    @Override
    public HttpHandlerRegistry getHandlerRegistry() {
        return handlerRegistry;
    }

    @Nonnull
    @Override
    public HttpAuthRegistry getAuthRegistry() {
        return authRegistry;
    }

    @Override
    public void applyUserAuth(@Nonnull HttpContext context, @Nonnull Pair<HttpAuthHandler, HttpAuthUser> values, @Nullable String header) {
        if (header != null) {
            String[] authorization = header.split(" ");

            if (authorization.length == 2) {
                String type = authorization[0];
                HttpAuthHandler authHandler = authRegistry.getAuthMethodHandler(type);
                values.setFirst(authHandler);

                if (authHandler != null) {
                    HttpAuthUser authUser = authHandler.getAuthUser(context, authorization[1]);
                    values.setSecond(authUser);
                }
            } else if (authorization.length == 1) {
                String token = authorization[0];
                HttpAuthHandler authHandler = authRegistry.getAuthMethodHandler("global");
                values.setFirst(authHandler);

                if (authHandler != null) {
                    HttpAuthUser authUser = authHandler.getAuthUser(context, token);
                    values.setSecond(authUser);
                }
            }
        }
    }
}
