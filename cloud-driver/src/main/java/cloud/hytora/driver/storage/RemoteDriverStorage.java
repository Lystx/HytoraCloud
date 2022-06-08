package cloud.hytora.driver.storage;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.packets.StorageUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Getter;

import javax.annotation.Nonnull;


@Getter
public class RemoteDriverStorage implements DriverStorage {

    /**
     * The raw data of this storage (document)
     */
    protected Document rawData;

    /**
     * The client that is the remote for this
     */
    private final ClusterParticipant client;

    public RemoteDriverStorage(ClusterParticipant client) {
        this.client = client;

        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<StorageUpdatePacket>) (channelHandlerContext, packet) -> {
            StorageUpdatePacket.StoragePayLoad payLoad = packet.getPayLoad();
            if (payLoad == StorageUpdatePacket.StoragePayLoad.UPDATE) {
                setRawData(packet.getStorage());
            }
        });
    }

    @Override
    @Nonnull
    public DriverStorage setRawData(@Nonnull Document rawData) {
        this.rawData = rawData;
        return this;
    }

    @Override
    public void update() {
        client.sendPacket(new StorageUpdatePacket(StorageUpdatePacket.StoragePayLoad.UPDATE, rawData));
    }

    @Override
    public void fetch() {
        this.rawData = client.getPacketChannel().prepareSingleQuery().execute(new StorageUpdatePacket(StorageUpdatePacket.StoragePayLoad.FETCH, rawData)).syncUninterruptedly().get().data();
        CloudDriver.getInstance().getLogger().info("Successfully fetched DriverStorage!");
    }
}
