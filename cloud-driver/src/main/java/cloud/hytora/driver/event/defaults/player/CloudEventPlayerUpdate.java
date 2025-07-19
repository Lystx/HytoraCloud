package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.entity.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudEventPlayerUpdate extends DefaultPlayerEvent {

    public CloudEventPlayerUpdate() {
        super();
    }

    public CloudEventPlayerUpdate(@NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }


}
