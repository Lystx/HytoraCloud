package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.packets.other.PacketDriverLogging;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.Remote;

public class RemoteLoggingHandler implements PacketHandler<PacketDriverLogging> {

    @Override
    public void handle(PacketChannel channel, PacketDriverLogging packet) {

        NetworkComponent component = packet.getComponent();
        String message = packet.getMessage();

        if (component.matches(Remote.getInstance().getExecutor())) {
            Remote.getInstance().getLogger().info(message);
        }
    }
}
