package cloud.hytora.driver.message;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.IHandlerNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class DefaultChannelMessenger implements IChannelMessenger {

    private final Map<String, ChannelMessageListener> cache;
    protected final IHandlerNetworkExecutor executor;

    public DefaultChannelMessenger() {
        this.executor = CloudDriver.getInstance().getNetworkExecutor();
        this.cache = new HashMap<>();

        executor.registerPacketHandler((PacketHandler<ChannelMessageExecutePacket>) (wrapper, packet) -> {
            ChannelMessage message = packet.getChannelMessage();

            NetworkComponent[] receivers = message.getReceivers();
            if (receivers == null || receivers.length == 0 || Arrays.stream(receivers).anyMatch(r -> r.matches(executor))) {
                String channel = message.getChannel();
                ChannelMessageListener handler = cache.get(channel);
                if (handler == null) {
                    return;
                }
                handler.handleIncoming(message);
            }
        });
    }

    @Override
    public void registerChannel(String channel, ChannelMessageListener consumer) {
        this.cache.put(channel, consumer);
    }

    @Override
    public void unregisterChannel(String channel) {
        this.cache.remove(channel);
    }

}
