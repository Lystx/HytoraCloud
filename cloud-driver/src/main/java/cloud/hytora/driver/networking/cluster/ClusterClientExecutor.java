package cloud.hytora.driver.networking.cluster;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import io.netty.channel.ChannelFutureListener;
import cloud.hytora.driver.networking.NetworkExecutor;


import io.netty.channel.Channel;

public interface ClusterClientExecutor extends NetworkExecutor {

    Channel getChannel();

    Document getData();

    String getName();

    boolean isAuthenticated();

    void setAuthenticated(boolean state);

    void setName(String name);

    default Wrapper<Boolean> close() {
        Wrapper<Boolean> wrapper = Wrapper.empty();
        getChannel().close().addListener(future -> {
            if (future.isSuccess()) {
                wrapper.setResult(true);
            } else {
                wrapper.setFailure(future.cause());
            }
        });
        return wrapper;
    }

    default void sendPacket(IPacket packet) {
        getChannel().writeAndFlush(packet).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }

}
