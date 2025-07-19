package cloud.hytora.driver.config;

import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface IDatabaseConfig extends IBufferObject {

    DatabaseType getType();

    void setType(DatabaseType type);

    String getHost();

    void setHost(String host);

    int getPort();

    void setPort(int port);

    String getDatabase();

    void setDatabase(String database);

    String getAuthDatabase();

    void setAuthDatabase(String database);

    String getUser();

    void setUser(String user);

    String getPassword();

    void setPassword(String user);


}
