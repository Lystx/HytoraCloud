package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.packets.StorageUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.storage.INetworkDocumentStorage;

public class NodeStoragePacketHandler implements PacketHandler<StorageUpdatePacket> {

    @Override
    public void handle(PacketChannel wrapper, StorageUpdatePacket packet) {
        StorageUpdatePacket.StoragePayLoad payLoad = packet.getPayLoad();
        if (payLoad == StorageUpdatePacket.StoragePayLoad.FETCH) {
            wrapper.prepareResponse().data(CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).getRawData()).execute(packet);
        } else {
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class).setRawData(packet.getStorage());
        }
    }
}
