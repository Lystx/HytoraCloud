package cloud.hytora.remote.impl.handler;

import cloud.hytora.driver.networking.packets.services.CloudServerCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.RemoteAdapter;

public class RemoteCommandHandler implements PacketHandler<CloudServerCommandPacket> {

    @Override
    public void handle(ChannelWrapper wrapper, CloudServerCommandPacket packet) {
        RemoteAdapter adapter = Remote.getInstance().getAdapter();
        if (adapter == null) {
            return;
        }
        adapter.executeCommand(packet.getCommand());
    }
}
