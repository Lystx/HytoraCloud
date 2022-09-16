package cloud.hytora.database.config;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.misc.RandomString;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class DatabaseConfig {

    private final String token;

    public DatabaseConfig() {
        this(null);
    }
    public DatabaseConfig(String token) {
        this.token = token == null ? new RandomString(10).nextString() : token;
    }
}
