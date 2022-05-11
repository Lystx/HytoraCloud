package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerDisconnectEvent extends DefaultPlayerEvent {

    public CloudPlayerDisconnectEvent(@NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
