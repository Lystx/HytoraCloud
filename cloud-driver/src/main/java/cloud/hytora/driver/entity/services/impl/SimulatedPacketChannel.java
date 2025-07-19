package cloud.hytora.driver.entity.services.impl;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.NetworkExecutor;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.packets.other.PacketRedirecting;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.networking.protocol.types.ConnectionState;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.protocol.wrapped.PacketAction;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.networking.protocol.wrapped.SimplePacketChannel;
import io.netty.channel.ChannelHandlerContext;
import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Optional;
import java.util.UUID;

@AllArgsConstructor
public class SimulatedPacketChannel extends SimplePacketChannel {

    private final CloudService service;

    @Override
    public ConnectionType getType() {
        return ConnectionType.SERVICE;
    }

    @Override
    public void log(String message, Object... args) {
        sendPacket(new PacketDriverLogging(NetworkComponent.of(service.getName(), ConnectionType.SERVICE), DriverUtility.args(message, args)));
    }

    @Override
    public void sendPacket(IPacket packet) {
        PacketRedirecting redirecting = new PacketRedirecting(service.getName(), packet);
        redirecting.publish();
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
    }

    @Override
    public boolean hasEverConnected() {
        return true;
    }

    @Override
    public String getName() {
        return service.getName();
    }

    @Override
    public UUID getUniqueId() {
        return service.getUniqueId();
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setUniqueId(UUID uniqueId) {}

    @Override
    public void setAuthenticated(boolean state) {}

    @Override
    public void setType(ConnectionType type) {}

    @Override
    public void setName(String name) {}

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    @Override
    public void flushPacket(IPacket packet) {
        this.sendPacket(packet);
    }

    @Override
    public void sendPacketSync(@NotNull IPacket packet) {
        Task.callSync(() -> {
            this.sendPacket(packet);
            return Void.TYPE;
        });
    }

    @Override
    public void close() {
    }

    @Override
    public ChannelHandlerContext context() {
        return null;
    }

    @Override
    public Optional<ChannelHandlerContext> optional() {
        return Optional.empty();
    }

    @Override
    public ConnectionState state() {
        return ConnectionState.CONNECTED;
    }

    @Override
    public NetworkExecutor executor() {
        return this;
    }

    @Override
    public long modificationTime() {
        return 0;
    }

    @Override
    public InetSocketAddress getClientAddress() {
        return new InetSocketAddress(service.getHostName(), service.getPort());
    }

    @Override
    public PacketChannel overrideExecutor(NetworkExecutor executor) {
        return this;
    }

    @Override
    public @NotNull PacketAction<Void> sendResponse() {
        return null;
    }

    @Override
    public @NotNull PacketAction<Void> sendResponse(IPacket packet) {
        return null;
    }

}
