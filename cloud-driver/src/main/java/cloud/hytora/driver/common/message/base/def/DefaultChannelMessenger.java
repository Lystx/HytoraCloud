package cloud.hytora.driver.common.message.base.def;

import cloud.hytora.common.Snowflake;
import cloud.hytora.document.Document;
import cloud.hytora.driver.common.message.base.ChannelMessenger;
import cloud.hytora.driver.common.message.DefaultMessageChannel;
import cloud.hytora.driver.common.message.IMessageChannel;
import cloud.hytora.driver.common.message.MessageListener;
import cloud.hytora.driver.networking.HandlingNetworkExecutor;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.other.PacketGenericChannelMessage;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DefaultChannelMessenger implements ChannelMessenger, PacketHandler<PacketGenericChannelMessage> {

    private final Map<Class<?>, Collection<IMessageChannel<?>>> registeredChannels;
    private final HandlingNetworkExecutor executor;

    public DefaultChannelMessenger(HandlingNetworkExecutor executor) {
        this.executor = executor;
        this.registeredChannels = new HashMap<>();

        executor.registerPacketHandler(this);
    }

    @Override
    public <T> @NotNull IMessageChannel<T> registerChannel(Class<T> typeClass, String name) {
        IMessageChannel<T> channel = new DefaultMessageChannel<>(typeClass, name, Snowflake.getInstance().nextId());
        Collection<IMessageChannel<?>> channels = this.registeredChannels.getOrDefault(typeClass, new ArrayList<>());
        channels.add(channel);
        this.registeredChannels.put(typeClass, channels);
        return channel;
    }

    @Override
    public @NotNull Collection<IMessageChannel<?>> getRegisteredChannels() {
        Collection<IMessageChannel<?>> channels = new ArrayList<>();
        for (Collection<IMessageChannel<?>> value : registeredChannels.values()) {
            channels.addAll(value);
        }
        return channels;
    }

    @Override
    public <T> IMessageChannel<T> getRegisteredChannel(Class<T> typeClass) {
        return (IMessageChannel<T>) getRegisteredChannels().stream().filter(c -> c.getTypeClass().equals(typeClass)).findFirst().orElse(null);
    }


    @Override
    public <T> IMessageChannel<T> getRegisteredChannel(String channelName) {
        return (IMessageChannel<T>) getRegisteredChannels().stream().filter(c -> c.getName().equalsIgnoreCase(channelName)).findFirst().orElse(null);
    }

    @Override
    public void unregisterChannel(String name) {
        IMessageChannel<Object> registeredChannel = getRegisteredChannel(name);
        if (registeredChannel == null) {
            return;
        }
        Collection<IMessageChannel<?>> channels = this.registeredChannels.get(registeredChannel.getTypeClass());
        channels.removeIf(c -> c.getSnowflake() == registeredChannel.getSnowflake());
        this.registeredChannels.put(registeredChannel.getTypeClass(), channels);
    }

    @Override
    public void handle(PacketChannel wrapper, PacketGenericChannelMessage packet) {
        Collection<NetworkComponent> receivers = packet.getReceivers();
        if (receivers.isEmpty() || receivers.stream().anyMatch(r -> r.getName().equalsIgnoreCase(executor.getName()))) {
            String channel = packet.getChannel();
            long startTime = packet.getStartTime();

            Object handleObject;
            if (!packet.isChannelMessageObject()) {
                String className = packet.getClassName();
                String jsonData = packet.getJsonData();

                try {
                    Class<?> objectClass = Class.forName(className);
                    Document document = Document.gson(jsonData);
                    handleObject = document.toInstance(objectClass);

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();;
                    handleObject = null;
                }

            } else {
                handleObject = packet.getChannelMessage();
            }

            IMessageChannel<Object> genericChannel = getRegisteredChannel(channel);
            if (genericChannel == null) {
                return;
            }
            for (MessageListener<Object> listener : ((DefaultMessageChannel<Object>) genericChannel).getListeners()) {
                try {
                    listener.handleMessage(handleObject, startTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

    }
}
