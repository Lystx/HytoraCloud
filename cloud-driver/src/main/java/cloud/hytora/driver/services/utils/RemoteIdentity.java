package cloud.hytora.driver.services.utils;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.HytoraCloudConstants;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.utils.version.VersionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RemoteIdentity implements IBufferObject {

    /**
     * The node auth key
     */
    private String authKey;

    /**
     * The name of the node this service runs on
     */
    private String node;

    /**
     * The version type
     */
    private VersionType versionType;

    /**
     * The processing of the service
     */
    private ServiceProcessType processType = ServiceProcessType.WRAPPER;

    /**
     * The logLevel of the cloud
     */
    private LogLevel logLevel = LogLevel.INFO;

    /**
     * The host name of the node this service runs on
     */
    private String hostname;

    /**
     * The name of this service to identify itself
     */
    private String name;

    /**
     * The port this service runs on
     */
    private int port;

    public void save(File file) {
        try {
            DocumentFactory.newJsonDocument(this).saveToFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static RemoteIdentity forApplication(ProtocolAddress address) {
        return new RemoteIdentity(address.getAuthKey(), "", VersionType.BUNGEE, ServiceProcessType.WRAPPER, LogLevel.INFO, address.getHost(), HytoraCloudConstants.APPLICATION_NAME, address.getPort());
    }

    public static RemoteIdentity read(File file) {
        return DocumentFactory.newJsonDocumentUnchecked(file).toInstance(RemoteIdentity.class);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.authKey = buf.readString();
                this.node = buf.readString();
                this.versionType = buf.readEnum(VersionType.class);
                this.processType = buf.readEnum(ServiceProcessType.class);
                this.logLevel = buf.readEnum(LogLevel.class);
                this.hostname = buf.readString();
                this.name = buf.readString();
                this.port = buf.readInt();
                break;

            case WRITE:
                buf.writeString(this.authKey);
                buf.writeString(this.node);
                buf.writeEnum(this.versionType);
                buf.writeEnum(this.processType);
                buf.writeEnum(this.logLevel);
                buf.writeString(this.hostname);
                buf.writeString(this.name);
                buf.writeInt(this.port);
                break;
        }
    }
}
