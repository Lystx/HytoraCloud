package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

public interface INodeConfig extends Bufferable {

    String getNodeName();

    String getAuthKey();

    String getBindAddress();

    ProtocolAddress[] getClusterAddresses();

    int getBindPort();

    boolean isRemote();

    void markAsRemote();

    JavaVersion[] getJavaVersions();
}
