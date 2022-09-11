package cloud.hytora.database.config;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.misc.RandomString;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class DatabaseConfig {

    private final String token;

    public DatabaseConfig() {
        this.token = new RandomString(10).nextString();
    }
}
