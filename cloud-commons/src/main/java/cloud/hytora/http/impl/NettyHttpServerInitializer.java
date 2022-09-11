package cloud.hytora.http.impl;

import cloud.hytora.http.HttpAddress;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import lombok.AllArgsConstructor;



@AllArgsConstructor
public class NettyHttpServerInitializer extends ChannelInitializer<Channel> {

	protected final NettyHttpServer server;
	protected final HttpAddress address;

	@Override
	protected void initChannel(Channel channel) throws Exception {
		channel.pipeline()
			.addLast("http-server-codec", new HttpServerCodec())
			.addLast("http-object-aggregator", new HttpObjectAggregator(Short.MAX_VALUE))
			.addLast("http-server-handler", new NettyHttpChannelHandler(server, address))
		;
	}
}
