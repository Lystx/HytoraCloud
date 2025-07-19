package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.entity.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudEventPlayerLoginFirstTime extends DefaultPlayerEvent {

    public CloudEventPlayerLoginFirstTime() {
        super();
    }

    public CloudEventPlayerLoginFirstTime(final @NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
