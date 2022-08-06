package cloud.hytora.driver.services.utils;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.services.utils.version.VersionType;
import cloud.hytora.driver.uuid.PlayerLoginProcessing;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;
import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class RemoteIdentity {

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
     * the processing of players
     */
    private PlayerLoginProcessing playerLoginProcessing = PlayerLoginProcessing.UUID_CACHE;

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
        return new RemoteIdentity(address.getAuthKey(), "", VersionType.BUNGEE, ServiceProcessType.WRAPPER, LogLevel.INFO, PlayerLoginProcessing.WAIT_FOR_UUID, address.getHost(), CloudDriver.APPLICATION_NAME, address.getPort());
    }

    public static RemoteIdentity read(File file) {
        return DocumentFactory.newJsonDocumentUnchecked(file).toInstance(RemoteIdentity.class);
    }
}
