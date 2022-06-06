package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.services.CloudServerCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;

public class RemoteCommandHandler implements PacketHandler<CloudServerCommandPacket> {

    @Override
    public void handle(PacketChannel wrapper, CloudServerCommandPacket packet) {
        RemoteAdapter adapter = Remote.getInstance().getAdapter();
        if (adapter == null) {
            return;
        }
        adapter.executeCommand(packet.getCommand());
    }
}
