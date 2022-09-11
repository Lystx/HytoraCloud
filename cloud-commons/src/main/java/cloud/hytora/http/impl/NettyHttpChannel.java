package cloud.hytora.http.impl;

import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.HttpChannel;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class NettyHttpChannel implements HttpChannel {

	protected final HttpAddress serverAddress, clientAddress;
	protected final Channel nettyChannel;

	@Override
	public void close() {
		nettyChannel.close();
	}

	@Override
	public String toString() {
		return "HttpChannel[client=" + clientAddress + " server=" + serverAddress + "]";
	}
}
