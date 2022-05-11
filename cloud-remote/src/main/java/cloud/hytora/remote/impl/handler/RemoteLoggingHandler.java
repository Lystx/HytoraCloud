package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.DriverLoggingPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.remote.Remote;

public class RemoteLoggingHandler implements PacketHandler<DriverLoggingPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, DriverLoggingPacket packet) {

        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.matches(Remote.getInstance().getExecutor())) {
            Remote.getInstance().getLogger().info(message);
        }
    }
}
