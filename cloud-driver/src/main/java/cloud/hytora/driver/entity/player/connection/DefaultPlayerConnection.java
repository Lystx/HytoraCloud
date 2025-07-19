package cloud.hytora.driver.entity.player.connection;

import cloud.hytora.driver.CloudDriver;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class DefaultPlayerConnection implements PlayerConnection, IBufferObject {

    private UUID connectionId;
    private String connectionName;
    private String proxyName;
    private ProtocolAddress address;
    private int rawVersion;
    private boolean onlineMode, legacy;

    @Nonnull
    public ProtocolVersion getVersion() {
        return ProtocolVersion.getVersion(rawVersion);
    }

    @Override
    public void disconnect(String reason) {
        CloudService proxy = CloudDriver.getInstance().getServiceManager().getCachedCloudService(proxyName);
        if (proxy != null) {
            proxy.sendPacket(
                    PacketCloudEntityPlayerExtension.forProxy(PacketCloudEntityPlayerExtension.ProxyPayLoad.PLAYER_EXECUTE_KICK, buffer -> buffer.writeUniqueId(connectionId).writeString(reason))
            );
        }
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
        switch (state) {
            case READ:
                connectionId = buffer.readOptionalUniqueId();
                connectionName = buffer.readOptionalString();
                proxyName = buffer.readOptionalString();
                address = buffer.readOptionalObject(ProtocolAddress.class);
                rawVersion = buffer.readInt();
                onlineMode = buffer.readBoolean();
                legacy = buffer.readBoolean();
                break;
            case WRITE:
                buffer.writeOptionalUniqueId(connectionId);
                buffer.writeOptionalString(connectionName);
                buffer.writeOptionalString(proxyName);
                buffer.writeOptionalObject(address);
                buffer.writeInt(rawVersion);
                buffer.writeBoolean(onlineMode);
                buffer.writeBoolean(legacy);
                break;
        }
    }
}
