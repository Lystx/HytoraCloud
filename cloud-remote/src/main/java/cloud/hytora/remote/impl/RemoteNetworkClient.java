package cloud.hytora.remote.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.remote.Remote;
import io.netty.channel.ChannelHandlerContext;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;

public class RemoteNetworkClient extends ClusterParticipant {

    public RemoteNetworkClient(String clientName, String hostname, int port, Document customData) {
        super(clientName, ConnectionType.SERVICE, customData);

        this.bootAsync().handlePacketsAsync().openConnection(hostname, port)
                .addUpdateListener(wrapper -> {
                    if (wrapper.isSuccess()) {
                        CloudDriver.getInstance().getLogger().info("The service start successfully network service.");
                    } else {
                        CloudDriver.getInstance().getLogger().info("The wrapper couldn't connect to the Node!");
                        wrapper.error().printStackTrace();
                    }
                });
    }

    @Override
    public void onAuthenticationChanged(ChannelWrapper wrapper) {
        CloudDriver.getInstance().getLogger().info("This service was authenticated by the cluster");

    }

    @Override
    public void onActivated(ChannelHandlerContext channelHandlerContext) {
        CloudDriver.getInstance().getLogger().info("This service successfully connected to the cluster.");
    }

    @Override
    public void onClose(ChannelHandlerContext channelHandlerContext) {
        CloudDriver.getInstance().getLogger().info("This service disconnected from the cluster");
    }

}
