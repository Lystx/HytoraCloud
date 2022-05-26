package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.StorageUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.ChannelWrapper;

public class NodeStoragePacketHandler implements PacketHandler<StorageUpdatePacket> {

    @Override
    public void handle(ChannelWrapper wrapper, StorageUpdatePacket packet) {
        StorageUpdatePacket.StoragePayLoad payLoad = packet.getPayLoad();
        if (payLoad == StorageUpdatePacket.StoragePayLoad.FETCH) {
            wrapper.prepareResponse().data(CloudDriver.getInstance().getStorage().getRawData()).execute(packet);
        } else {
            CloudDriver.getInstance().getStorage().setRawData(packet.getStorage());
        }
    }
}
