package cloud.hytora.driver.node.config;

import cloud.hytora.driver.http.SSLConfiguration;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@Getter
@NoArgsConstructor
@Setter
public class DefaultNodeConfig implements INodeConfig {

    private String nodeName;
    private String authKey;
    private String bindAddress;
    private int bindPort;
    private boolean remote;

    private int maxBootableServicesAtSameTime;

    private ServiceCrashPrevention serviceCrashPrevention;

    private Collection<SimpleJavaVersion> javaVersions;
    private ProtocolAddress[] clusterAddresses;
    private ProtocolAddress[] httpListeners;
    private SSLConfiguration sslConfiguration;

    public Collection<JavaVersion> getJavaVersions() {
        return new ArrayList<>(javaVersions);
    }

    @Override
    public void markAsRemote() {
        this.remote = true;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                nodeName = buf.readString();
                authKey = buf.readString();
                bindAddress = buf.readString();
                maxBootableServicesAtSameTime = buf.readInt();
                clusterAddresses = buf.readObjectArray(ProtocolAddress.class);
                bindPort = buf.readInt();
                remote = buf.readBoolean();
                serviceCrashPrevention = buf.readObject(ServiceCrashPrevention.class);
                javaVersions = buf.readObjectCollection(SimpleJavaVersion.class);
                break;

            case WRITE:
                buf.writeString(nodeName);
                buf.writeString(authKey);
                buf.writeString(bindAddress);
                buf.writeInt(maxBootableServicesAtSameTime);
                buf.writeObjectArray(clusterAddresses);
                buf.writeInt(bindPort);
                buf.writeBoolean(remote);
                buf.writeObject(serviceCrashPrevention);
                buf.writeObjectCollection(javaVersions);
                break;
        }
    }

}
