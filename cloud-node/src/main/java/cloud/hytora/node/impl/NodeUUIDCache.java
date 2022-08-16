package cloud.hytora.node.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.uuid.DriverUUIDCache;
import cloud.hytora.driver.uuid.packets.CachedUUIDPacket;
import cloud.hytora.node.NodeDriver;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class NodeUUIDCache implements DriverUUIDCache {

    private boolean enabled;

    private Map<String, UUID> cachedUniqueIds;

    private final File cacheFile;

    public NodeUUIDCache() {
        this.cachedUniqueIds = new ConcurrentHashMap<>();

        this.cacheFile = new File(NodeDriver.STORAGE_FOLDER, "cache.json");

        NodeDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<CachedUUIDPacket>) (wrapper, packet) -> {
            PacketBuffer buffer = packet.buffer();
            switch (buffer.readEnum(CachedUUIDPacket.PayLoad.class)) {
                case LOAD_CACHE:

                    wrapper.prepareResponse()
                            .buffer(buf -> {
                                buf.writeInt(cachedUniqueIds.size());
                                for (String s : cachedUniqueIds.keySet()) {
                                    buf.writeString(s);
                                    buf.writeUniqueId(cachedUniqueIds.get(s));
                                }
                            }).execute(packet);
                    break;

                case UPDATE_CACHE:

                    int size = buffer.readInt();
                    cachedUniqueIds = new ConcurrentHashMap<>(size);

                    for (int i = 0; i < size; i++) {
                        cachedUniqueIds.put(buffer.readString(), buffer.readUniqueId());
                    }
                    break;
            }
        });
    }

    @Override
    public Task<Collection<UUID>> loadAsync() {
        return Task.callAsync(() -> {
            Document document = DocumentFactory.newJsonDocument(this.cacheFile);

            for (String key : document.keys()) {
                UUID uuid = document.get(key).toUniqueId();

                this.cachedUniqueIds.put(key, uuid);
            }


            return getCacheLoadedUniqueIds();
        });
    }

    @Override
    public void setUUID(String name, UUID uuid) {
        this.cachedUniqueIds.put(name, uuid);
    }

    @Override
    public UUID getUUID(String name) {
        return this.cachedUniqueIds.get(name);
    }

    @Override
    public void update() {
        Document document = DocumentFactory.newJsonDocument();
        document.set(this.cachedUniqueIds);
        try {
            document.saveToFile(this.cacheFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        new CachedUUIDPacket(CachedUUIDPacket.PayLoad.UPDATE_CACHE, buf -> {
            buf.writeInt(this.cachedUniqueIds.size());
            for (String s : this.cachedUniqueIds.keySet()) {
                buf.writeString(s);
                buf.writeUniqueId(this.cachedUniqueIds.get(s));
            }
        }).publishAsync();
    }

    @Override
    public Collection<UUID> getCacheLoadedUniqueIds() {
        return this.cachedUniqueIds.values();
    }
}
