package cloud.hytora.driver.http.impl;

import cloud.hytora.driver.http.api.HttpChannel;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import io.netty.channel.Channel;
import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class NettyHttpChannel implements HttpChannel {

	protected final ProtocolAddress serverAddress, clientAddress;
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
