package cloud.hytora.driver.http.impl;

import cloud.hytora.common.collection.pair.Tuple;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.http.api.*;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpRequest;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;


@RequiredArgsConstructor
public class NettyHttpChannelHandler extends SimpleChannelInboundHandler<HttpRequest>  {

	protected final NettyHttpServer server;
	protected final ProtocolAddress address;

	protected NettyHttpChannel channel;

	@Override
	public void channelActive(ChannelHandlerContext context) throws Exception {
		channel = new NettyHttpChannel(address, new ProtocolAddress((InetSocketAddress) context.channel().remoteAddress()), context.channel());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext context, Throwable ex) throws Exception {
		if (!(ex instanceof IOException)) {
			CloudDriver.getInstance().getLogger().error("Error on channel {}", channel, ex);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext context, HttpRequest request) throws Exception {
		if (request.decoderResult().isFailure()) {
			context.close();
			return;
		}

		URI uri = URI.create(request.uri());

		String fullPath = uri.getPath();
		CloudDriver.getInstance().getLogger().trace("Received {} on route '{}'", request.method(), fullPath);

		Map<String, String> pathParameters = new LinkedHashMap<>();
		NettyHttpContext httpContext = new NettyHttpContext(uri, channel, server, request, context.channel(), pathParameters);

		HttpMethod method = httpContext.getRequest().getMethod();
		String[] pathEntries = fullPath.split("/");
		String[] handlerPathEntries;
		Tuple<HttpAuthHandler, HttpAuthUser> auth = null;
		Collection<RegisteredHandler> pathHandlers = new ArrayList<>();
		for (RegisteredHandler handler : server.getHandlerRegistry().getHandlers()) {

			handlerPathEntries = handler.getPath().split("/");
			if (!checkPath(pathEntries, handlerPathEntries, pathParameters)) continue;
			pathHandlers.add(handler);
			if (!Arrays.asList(handler.getMethods()).contains(method)) continue;

			if (!handler.getPermission().isEmpty()) {
				if (auth == null) {
					auth = Tuple.empty();
					String header = httpContext.getRequest().getHeader("Authorization");
					server.applyUserAuth(auth, header);
				}

				if (auth.getSecond() == null) {
					httpContext.getResponse().setStatusCode(HttpCodes.UNAUTHORIZED);
					break;
				}
				if (!auth.getSecond().hasPermission(handler.getPermission())) {
					httpContext.getResponse().setStatusCode(HttpCodes.FORBIDDEN).setBody("Permission " + handler.getPermission() + " required");
					break;
				}

			}

			try {
				handler.execute(httpContext);
			} catch (Exception ex) {
				httpContext.getResponse().setStatusCode(HttpCodes.INTERNAL_SERVER_ERROR);
				CloudDriver.getInstance().getLogger().error("Could not execute http handler for '{}'", fullPath, ex);
			}

			if (httpContext.cancelNext) break;

		}

		if (!httpContext.cancelSendResponse) {

			// CORS
			if (!httpContext.getResponse().hasHeader("Access-Control-Allow-Origin"))
				httpContext.getResponse().setHeader("Access-Control-Allow-Origin", "*");
			if (!httpContext.getResponse().hasHeader("Access-Control-Allow-Methods")) {
				Set<String> methods = new LinkedHashSet<>();
				for (RegisteredHandler handler : pathHandlers) {
					for (HttpMethod current : handler.getMethods()) {
						methods.add(current.name());
					}
				}
				methods.add("OPTIONS");
				httpContext.getResponse().setHeader("Access-Control-Allow-Methods", String.join(", ", methods));
				if (httpContext.getRequest().getMethod() == HttpMethod.OPTIONS && httpContext.getResponse().getStatusCode() == HttpCodes.NOT_FOUND) {
					httpContext.getResponse().setStatusCode(HttpCodes.OK);
				}
			}
			if (!httpContext.getResponse().hasHeader("Access-Control-Allow-Headers"))
				httpContext.getResponse().setHeader("Access-Control-Allow-Headers", "*");

			// Send default message
			if (httpContext.response.nettyResponse.content().readableBytes() == 0) {
				int code = httpContext.getResponse().getStatusCode();

				if (!HttpCodes.isSuccessful(code) && !HttpCodes.isRedirection(code)) {
					final String message;
					switch (code) {
						case HttpCodes.NOT_FOUND:
							message = "Cannot " + method + " " + fullPath;
							break;
						default:
							message = HttpCodes.getStatusMessage(code);
					}

					httpContext.getResponse().setBody((code + ": " + message).trim());
				}
			}

			ChannelFuture channelFuture = context.channel().writeAndFlush(httpContext.response.nettyResponse).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);

			if (httpContext.closeAfter) {
				channelFuture.addListener(ChannelFutureListener.CLOSE);
			}
		}

	}

	private boolean checkPath(@Nonnull String[] pathEntries, @Nonnull String[] handlerPathEntries, @Nonnull Map<String, String> pathParameters) {

		if (pathEntries.length != handlerPathEntries.length)
			return false;

		for (int i = 0; i < handlerPathEntries.length; i++) {

			String handlerPathEntry = handlerPathEntries[i];
			String providedPathEntry = pathEntries[i];

			if (handlerPathEntry.equals("*"))
				continue;
			if (handlerPathEntry.startsWith("{") && handlerPathEntry.endsWith("}")) {
				String pathArgumentName = handlerPathEntry.substring(1, handlerPathEntry.length() - 1);
				pathParameters.put(pathArgumentName, providedPathEntry);
				continue;
			}

			if (!handlerPathEntry.equalsIgnoreCase(providedPathEntry))
				return false;

		}

		return true;
	}
}
