package cloud.hytora.driver.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface IConfig extends IBufferObject {


    /**
     * Updates the config and syncs it over the network
     */
    void update();

    IDatabaseConfig getDatabaseConfig();

    INodeConfig getNodeConfig();

    void setDatabaseConfig(IDatabaseConfig config);
    void setNodeConfig(INodeConfig config);
}
