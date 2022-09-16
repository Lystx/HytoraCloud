package cloud.hytora.driver.networking.cluster.client;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.cluster.ClusterClientExecutor;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import io.netty.channel.Channel;

import io.netty.channel.ChannelFutureListener;
import lombok.Getter;
import lombok.Setter;

import java.nio.channels.ClosedChannelException;

@Getter
@Setter
public class SimpleClusterClientExecutor extends SimpleNetworkComponent implements ClusterClientExecutor {


    private final Channel channel;
    private Document data;
    private boolean authenticated;

    public SimpleClusterClientExecutor(Channel channel) {
        super("UNKNOWN", ConnectionType.UNKNOWN);
        this.channel = channel;
        this.authenticated = false;
        this.name = "UNKNOWN";
        this.data = DocumentFactory.newJsonDocument();
    }

    public void sendPacket(IPacket packet) {
        if (channel.isOpen()) {
            channel.writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
    }

    @Override
    public Task<Void> sendPacketAsync(IPacket packet) {
        Task<Void> task = Task.empty();
        if (channel.isOpen()) {
            channel
                    .writeAndFlush(packet)
                    .addListener(future -> {
                        if (future.isSuccess()) {
                            task.setResult(null);
                        } else {
                            task.setFailure(future.cause());
                        }
                    });
        } else {
            task.setFailure(new ClosedChannelException());
        }

        return task;
    }

}
