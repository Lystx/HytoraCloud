package cloud.hytora.driver.node.config;

import cloud.hytora.driver.http.SSLConfiguration;
import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.Collection;

public interface INodeConfig extends IBufferObject {

    String getNodeName();

    String getAuthKey();

    String getBindAddress();

    int getMaxBootableServicesAtSameTime();

    ProtocolAddress[] getHttpListeners();

    ServiceCrashPrevention getServiceCrashPrevention();

    SSLConfiguration getSslConfiguration();

    ProtocolAddress[] getClusterAddresses();

    int getBindPort();

    boolean isRemote();

    void markAsRemote();

    Collection<JavaVersion> getJavaVersions();
}
