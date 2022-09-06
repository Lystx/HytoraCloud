package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.packets.defaults.DriverLoggingPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.Remote;

public class RemoteLoggingHandler implements PacketHandler<DriverLoggingPacket> {

    @Override
    public void handle(PacketChannel wrapper, DriverLoggingPacket packet) {

        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.matches(Remote.getInstance().getNetworkExecutor())) {
            Remote.getInstance().getLogger().info(message);
        }
    }
}
