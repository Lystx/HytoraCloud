package cloud.hytora.remote.impl;

import cloud.hytora.common.function.ExceptionallyFunction;
import cloud.hytora.common.task.ITask;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferedResponse;
import cloud.hytora.driver.networking.protocol.packets.IPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.uuid.IdentificationCache;
import cloud.hytora.driver.uuid.packets.CachedUUIDPacket;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Getter @Setter
public class RemoteIdentificationCache implements IdentificationCache {

    private boolean enabled;

    private Map<String, UUID> cachedUUIDs;

    public RemoteIdentificationCache() {
        this.cachedUUIDs = new ConcurrentHashMap<>();

        CloudDriver.getInstance().getNetworkExecutor().registerPacketHandler((PacketHandler<CachedUUIDPacket>) (wrapper, packet) -> {
            if (packet.buffer().readEnum(CachedUUIDPacket.PayLoad.class) == CachedUUIDPacket.PayLoad.UPDATE_CACHE) {

                PacketBuffer buffer = packet.buffer();
                this.readCache(buffer);
            }
        });
    }

    private void readCache(PacketBuffer buffer) {

        int size = buffer.readInt();
        cachedUUIDs = new ConcurrentHashMap<>(size);

        for (int i = 0; i < size; i++) {
            cachedUUIDs.put(buffer.readString(), buffer.readUniqueId());
        }
    }

    @Override
    public ITask<Collection<UUID>> loadAsync() {
        if (!isEnabled()) ITask.newInstance(new LinkedList<>());
        IPacket packet = new CachedUUIDPacket(CachedUUIDPacket.PayLoad.LOAD_CACHE, buffer -> {});

        return packet
                .awaitResponse()
                .map((ExceptionallyFunction<BufferedResponse, Collection<UUID>>) bufferedResponse -> {
                    PacketBuffer buffer = bufferedResponse.buffer();
                    this.readCache(buffer);
                    return this.cachedUUIDs.values();
                });
    }

    @Override
    public void setUUID(String name, UUID uuid) {
        if (!isEnabled()) return;
        this.cachedUUIDs.put(name, uuid);
    }

    @Override
    public UUID getUUID(String name) {
        if (!isEnabled()) return null;
        return this.cachedUUIDs.get(name);
    }

    @Override
    public void update() {
        if (!isEnabled()) return;
        new CachedUUIDPacket(CachedUUIDPacket.PayLoad.UPDATE_CACHE, buf -> {

            buf.writeInt(this.cachedUUIDs.size());
            for (String s : this.cachedUUIDs.keySet()) {
                buf.writeString(s);
                buf.writeUniqueId(this.cachedUUIDs.get(s));
            }
        }).publishAsync();
    }

    @Override
    public Collection<UUID> getCacheLoadedUniqueIds() {
        return cachedUUIDs.values();
    }
}
