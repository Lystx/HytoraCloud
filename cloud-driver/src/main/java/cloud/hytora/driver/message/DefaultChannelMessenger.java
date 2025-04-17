package cloud.hytora.driver.message;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.message.packet.ChannelMessageExecutePacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Getter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public abstract class DefaultChannelMessenger implements ChannelMessenger {

    private final Map<String, ChannelMessageListener> cache;
    protected final AdvancedNetworkExecutor executor;

    public DefaultChannelMessenger(AdvancedNetworkExecutor executor) {
        this.executor = executor;
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
    public void sendDocumentPacket(DocumentPacket packet) {
        this.sendDoc(packet, null);
    }

    @Override
    public void sendDocumentPacket(DocumentPacket packet, NetworkComponent[] receivers) {
        for (NetworkComponent receiver : receivers) {
            sendDoc(packet, receiver);
        }
    }

    private void sendDoc(DocumentPacket packet, NetworkComponent receiver) {
        Document data = Document.newJsonDocument();
        packet.handleData(BufferState.WRITE, data);
        ChannelMessage message = new ChannelMessageBuilder()
                .channel(packet.getChannel())
                .key(packet.getClass().getName())
                .document(data)
                .build();

        this.sendChannelMessage(message);
    }

    @Override
    public <T extends DocumentPacket> void registerPacketChannel(String channel, Consumer<T> handler) {
        this.registerChannel(channel, new ChannelMessageListener() {
            @Override
            public void handleIncoming(ChannelMessage message) {
                try {
                    String className = message.getKey();
                    T packet = (T) ReflectionUtils.createEmpty(Class.forName(className));
                    packet.handleData(BufferState.READ, message.getDocument());
                    handler.accept(packet);
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
