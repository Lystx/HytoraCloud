package cloud.hytora.node.impl.database.cloud;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DatabaseConfiguration {

    private DatabaseType type;
    private String host;
    private int port;
    private String database;
    private String authDatabase;
    private String user;
    private String password;

}
