package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.ICloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerUpdateEvent extends DefaultPlayerEvent {

    public CloudPlayerUpdateEvent(final @NotNull ICloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
