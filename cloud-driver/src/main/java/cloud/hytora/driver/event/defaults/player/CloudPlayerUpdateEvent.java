package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerUpdateEvent extends DefaultPlayerEvent {

    public CloudPlayerUpdateEvent(final @NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
