package cloud.hytora.remote.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.remote.RemoteConnectEvent;
import cloud.hytora.driver.networking.cluster.client.AdvancedClusterParticipant;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.packets.AuthenticationPacket;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;
import io.netty.channel.ChannelHandlerContext;

import java.util.concurrent.TimeUnit;

public class RemoteNetworkClient extends AdvancedClusterParticipant {

    public RemoteNetworkClient(String authKey, String clientName, Document customData) {
        super(authKey, clientName, ConnectionType.SERVICE, customData);
    }

    @Override
    public void sendPacket(IPacket packet) {
        super.sendPacket(packet);
    }

    @Override
    public void onAuthenticationChanged(PacketChannel wrapper) {
        CloudDriver.getInstance().getLogger().info("This service has shaken hands with the Node");
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
