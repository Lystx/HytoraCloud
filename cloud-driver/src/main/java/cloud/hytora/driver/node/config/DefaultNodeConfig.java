package cloud.hytora.driver.node.config;

import cloud.hytora.http.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class DefaultNodeConfig implements INodeConfig {

    private String nodeName;

    private UUID uniqueId;

    private ProtocolAddress address;

    private String authKey;

    private boolean remote;

    private int maxBootableServicesAtSameTime;
    private long memory;
    private ProtocolAddress[] clusterAddresses;

    @Override
    public void setRemote() {
        this.remote = true;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                nodeName = buf.readString();
                uniqueId = buf.readUniqueId();
                address = buf.readAddress();
                authKey = buf.readString();
                remote = buf.readBoolean();
                maxBootableServicesAtSameTime = buf.readInt();
                memory = buf.readLong();
                int size = buf.readInt();
                clusterAddresses = new ProtocolAddress[size];
                for (int i = 0; i < size; i++) {
                    clusterAddresses[i] = buf.readAddress();
                }
                break;

            case WRITE:
                buf.writeString(nodeName);
                buf.writeUniqueId(uniqueId);
                buf.writeAddress(address);
                buf.writeString(authKey);
                buf.writeBoolean(remote);
                buf.writeInt(maxBootableServicesAtSameTime);
                buf.writeLong(memory);

                buf.writeInt(clusterAddresses.length);
                for (ProtocolAddress clusterAddress : clusterAddresses) {
                    buf.writeAddress(clusterAddress);
                }
                break;
        }
    }

}
