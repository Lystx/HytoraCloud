package cloud.hytora.http.impl;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.http.api.WebSocketFrameType;
import cloud.hytora.http.api.WebSocketListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.*;

import javax.annotation.Nonnull;


public class NettyWebSocketChannelHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

	protected final NettyWebSocketChannel channel;

	public NettyWebSocketChannelHandler(@Nonnull NettyWebSocketChannel channel) {
		this.channel = channel;
	}

	@Override
	public void channelActive(ChannelHandlerContext context) throws Exception {
		channel.context.server.getWebSocketChannels().add(channel);
		Logger.constantInstance().info("{} was successfully connected!", channel);
	}

	@Override
	public void channelInactive(ChannelHandlerContext context) throws Exception {
		channel.context.server.getWebSocketChannels().remove(channel);
		Logger.constantInstance().info("{} was closed", channel);
	}

	@Override
	protected void channelRead0(ChannelHandlerContext channelHandlerContext, WebSocketFrame frame) throws Exception {
		if (frame instanceof PingWebSocketFrame) {
			handle(WebSocketFrameType.PING, frame);
		}
		if (frame instanceof PongWebSocketFrame) {
			handle(WebSocketFrameType.PONG, frame);
		}
		if (frame instanceof TextWebSocketFrame) {
			handle(WebSocketFrameType.TEXT, frame);
		}
		if (frame instanceof BinaryWebSocketFrame) {
			handle(WebSocketFrameType.BINARY, frame);
		}
		if (frame instanceof CloseWebSocketFrame) {
			handle(WebSocketFrameType.CLOSE, frame);
			channel.close(1000, "client connection closed");
		}
	}

	public void handle(@Nonnull WebSocketFrameType type, @Nonnull WebSocketFrame frame) {
		Logger.constantInstance().trace("Received {} on {}", type, channel);

		byte[] data = NettyUtils.asByteArray(frame.content());

		for (WebSocketListener listener : channel.getListeners()) {
			try {
				listener.handle(channel, type, data);
			} catch (Exception ex) {
				Logger.constantInstance().error("An error occurred while executing websocket listener", ex);
			}
		}
	}
}
