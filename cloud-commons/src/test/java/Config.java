import cloud.hytora.context.annotations.DocumentConfiguration;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@DocumentConfiguration("test.json")
public class Config {

    private String version;
    private boolean colored;
    private int maxTicksPerSecond;
    private List<Player> cachedPlayers;
    private Set<UUID> cachedUniqueIds;
}
