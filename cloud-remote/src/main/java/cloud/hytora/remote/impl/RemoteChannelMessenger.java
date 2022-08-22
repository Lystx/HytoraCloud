package cloud.hytora.remote.impl;

import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.message.DefaultChannelMessenger;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.protocol.wrapped.ChanneledPacketAction;

import java.util.Arrays;

public class RemoteChannelMessenger extends DefaultChannelMessenger {


    @Override
    public void sendChannelMessage(ChannelMessage message, NetworkComponent[] receivers) {
        PacketChannel wrapper = this.executor.getPacketChannel();

        ChanneledPacketAction<Void> transfer = wrapper.prepareTransfer();

        //declare receiver names and types
        transfer.receivers(Arrays.stream(receivers).map(NetworkComponent::getName).toArray(String[]::new));
        transfer.receivers(Arrays.stream(receivers).map(NetworkComponent::getType).toArray(ConnectionType[]::new));

        //send packet
        transfer.execute(new ChannelMessageExecutePacket(message));
    }
}
