package cloud.hytora.driver.http.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.http.api.HttpChannel;
import cloud.hytora.driver.http.api.HttpServer;
import cloud.hytora.driver.http.api.WebSocketChannel;
import cloud.hytora.driver.http.api.WebSocketFrameType;
import cloud.hytora.driver.http.api.WebSocketListener;
import com.google.common.base.Preconditions;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.websocketx.*;

import javax.annotation.Nonnull;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;


public class NettyWebSocketChannel implements WebSocketChannel {

	protected final Collection<WebSocketListener> listeners = new CopyOnWriteArrayList<>();
	protected final NettyHttpContext context;
	protected final Channel nettyChannel;

	public NettyWebSocketChannel(@Nonnull NettyHttpContext context, @Nonnull Channel nettyChannel) {
		this.context = context;
		this.nettyChannel = nettyChannel;
	}

	@Override
	public void sendFrame(@Nonnull WebSocketFrameType type, @Nonnull String text) {
		sendFrame(type, text.getBytes(StandardCharsets.UTF_8));
	}

	@Override
	public void sendFrame(@Nonnull WebSocketFrameType type, @Nonnull byte[] data) {
		Preconditions.checkNotNull(type);
		Preconditions.checkNotNull(data);

		WebSocketFrame frame;
		CloudDriver.getInstance().getLogger().trace("Sending {} on {}", type, this);

		switch (type) {
			case PING:
				frame = new PingWebSocketFrame(Unpooled.buffer(data.length).writeBytes(data));
				break;
			case PONG:
				frame = new PongWebSocketFrame(Unpooled.buffer(data.length).writeBytes(data));
				break;
			case TEXT:
				frame = new TextWebSocketFrame(Unpooled.buffer(data.length).writeBytes(data));
				break;
			case BINARY:
				frame = new BinaryWebSocketFrame(Unpooled.buffer(data.length).writeBytes(data));
				break;
			default:
				throw new IllegalArgumentException("Cannot send " + type + " frame");
		}

		nettyChannel.writeAndFlush(frame).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
	}

	@Override
	public void close() {
		close(1000, "websocket closed");
	}

	@Override
	public void close(int statusCode, @Nonnull String closeReason) {
		nettyChannel.writeAndFlush(new CloseWebSocketFrame(statusCode, closeReason)).addListener(ChannelFutureListener.CLOSE);
	}

	@Nonnull
	@Override
	public HttpChannel getChannel() {
		return context.getChannel();
	}

	@Nonnull
	@Override
	public HttpServer getServer() {
		return context.getServer();
	}

	@Override
	public void addListener(@Nonnull WebSocketListener listener) {
		listeners.add(listener);
	}

	@Override
	public void clearListeners() {
		listeners.clear();
	}

	@Nonnull
	@Override
	public Collection<WebSocketListener> getListeners() {
		return listeners;
	}

	@Override
	public String toString() {
		return "WebSocketChannel[client=" + getChannel().getClientAddress() + " server=" + getChannel().getServerAddress() + "]";
	}
}
