package cloud.hytora.driver.storage;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.cluster.client.ClusterParticipant;
import cloud.hytora.driver.networking.protocol.packets.defaults.StorageUpdatePacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import lombok.Getter;

import javax.annotation.Nonnull;


@Getter
public class RemoteNetworkDocumentStorage implements INetworkDocumentStorage {

    /**
     * The raw data of this storage (document)
     */
    protected Document rawData;

    /**
     * The client that is the remote for this
     */
    private final ClusterParticipant client;

    public RemoteNetworkDocumentStorage(ClusterParticipant client) {
        this.client = client;

        CloudDriver.getInstance().getNetworkExecutor().registerPacketHandler((PacketHandler<StorageUpdatePacket>) (channelHandlerContext, packet) -> {
            StorageUpdatePacket.StoragePayLoad payLoad = packet.getPayLoad();
            if (payLoad == StorageUpdatePacket.StoragePayLoad.UPDATE) {
                setRawData(packet.getStorage());
            }
        });
    }

    @Override
    @Nonnull
    public INetworkDocumentStorage setRawData(@Nonnull Document rawData) {
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

    @Override
    public Task<Document> fetchAsync() {
        Task<Document> task = Task.empty();

        client.getPacketChannel().prepareSingleQuery().execute(new StorageUpdatePacket(StorageUpdatePacket.StoragePayLoad.FETCH, rawData)).onTaskSucess(bufferedResponse -> {
            task.setResult(bufferedResponse.data());
        }).onTaskFailed(task::setFailure);
        return task;
    }
}
