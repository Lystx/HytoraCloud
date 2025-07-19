package cloud.hytora.driver.common.message;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.Cluster;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.other.PacketGenericChannelMessage;
import cloud.hytora.driver.networking.protocol.SimpleNetworkComponent;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
@AllArgsConstructor
public class DefaultMessageChannel<T> implements IMessageChannel<T> {

    private final Class<?> typeClass;
    private final String name;
    private final long snowflake;
    private final Collection<MessageListener<T>> listeners = new ArrayList<>();


    @Override
    public void sendMessage(T t, NetworkComponent... receiver) {
        PacketGenericChannelMessage packet;
        if (receiver.length == 0) {
            packet = new PacketGenericChannelMessage(name, t);
        }  else {
            packet = new PacketGenericChannelMessage(name, t, receiver);
        }


        if (CloudDriver.getInstance().getEnvironment() == CloudDriver.Environment.NODE) {
            Cluster executor = (Cluster) CloudDriver.getInstance().getExecutor();
            if (receiver.length > 0) {
                for (NetworkComponent messageReceiver : receiver) {
                    if (messageReceiver == null) {
                        continue;
                    }
                    PacketChannel client = executor.getConnectedChannel(messageReceiver.getName());
                    if (client == null) {
                        return;
                    }
                    client.sendPacket(packet);
                }
            } else {
                executor.sendPacketToAll(packet);
            }
        } else {
            packet.publishToAll();
        }

    }

    @Override
    public void sendMessage(T t) {
        sendMessage(t, new SimpleNetworkComponent[0]);
    }

    @Override
    public void registerListener(MessageListener<T> listener) {
        this.listeners.add(listener);
    }


    @Override
    public void unregister() {
        this.listeners.clear();
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(this.name);
    }
}
