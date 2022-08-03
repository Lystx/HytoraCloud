package cloud.hytora.driver.message;

import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class DefaultChannelMessenger implements ChannelMessenger {

    private final Map<String, Consumer<ChannelMessage>> cache;
    protected final AdvancedNetworkExecutor executor;

    public DefaultChannelMessenger(AdvancedNetworkExecutor executor) {
        this.executor = executor;
        this.cache = new HashMap<>();

        executor.registerPacketHandler((PacketHandler<ChannelMessageExecutePacket>) (wrapper, packet) -> {
            ChannelMessage message = packet.getChannelMessage();

            NetworkComponent[] receivers = message.getReceivers();
            if (receivers == null || Arrays.stream(receivers).anyMatch(r -> r.matches(executor))) {
                String channel = message.getChannel();
                Consumer<ChannelMessage> handler = cache.get(channel);
                handler.accept(message);
            }
        });
    }

    @Override
    public void registerChannel(String channel, Consumer<ChannelMessage> consumer) {
        this.cache.put(channel, consumer);
    }

    @Override
    public void unregisterChannel(String channel) {
        this.cache.remove(channel);
    }

}
