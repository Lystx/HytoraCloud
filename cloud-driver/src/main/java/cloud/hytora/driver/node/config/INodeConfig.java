package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.ProtocolAddress;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.util.UUID;

public interface INodeConfig extends IBufferObject {

    String getNodeName();

    UUID getUniqueId();

    ProtocolAddress getAddress();

    String getAuthKey();

    int getMaxBootableServicesAtSameTime();

    long getMemory();


    ProtocolAddress[] getClusterAddresses();


    boolean isRemote();

    void setRemote();

}
