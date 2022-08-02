package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.ICloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerLoginEvent extends DefaultPlayerEvent {

    public CloudPlayerLoginEvent(final @NotNull ICloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
