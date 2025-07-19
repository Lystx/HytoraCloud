package cloud.hytora.driver.config.def;

import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.config.IDatabaseConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UniversalDatabaseConfig implements IDatabaseConfig {

    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String authDatabase;
    private String user;
    private String password;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                type = buf.readEnum(DatabaseType.class);
                host = buf.readOptionalString();
                port = buf.readInt();
                database = buf.readOptionalString();
                authDatabase = buf.readOptionalString();
                user = buf.readOptionalString();
                password = buf.readOptionalString();
                break;

            case WRITE:
                buf.writeEnum(type);
                buf.writeOptionalString(host);
                buf.writeInt(port);
                buf.writeOptionalString(database);
                buf.writeOptionalString(authDatabase);
                buf.writeOptionalString(user);
                buf.writeOptionalString(password);
                break;
        }
    }
}
