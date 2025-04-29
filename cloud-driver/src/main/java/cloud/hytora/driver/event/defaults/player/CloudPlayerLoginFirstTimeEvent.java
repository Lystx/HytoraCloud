package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.ICloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerLoginFirstTimeEvent extends DefaultPlayerEvent {

    public CloudPlayerLoginFirstTimeEvent(final @NotNull ICloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
