package cloud.hytora.remote.impl;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.cluster.client.AdvancedClusterParticipant;
import cloud.hytora.driver.networking.protocol.packets.*;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import io.netty.channel.ChannelHandlerContext;

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
        DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §6HANDSHAKE");
    }

    @Override
    public void onActivated(ChannelHandlerContext channelHandlerContext) {
        DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §aCONNECTED");
    }


    @Override
    public void onClose(ChannelHandlerContext channelHandlerContext) {
        DriverUtility.printColored("Remote", "%1Service§8-%1To§8-%1Node %2Status§8: §cDISCONNECTED");
    }

}
