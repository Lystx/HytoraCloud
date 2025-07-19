package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.entity.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudEventPlayerLoginSuccess extends DefaultPlayerEvent {

    public CloudEventPlayerLoginSuccess() {
        super();
    }

    public CloudEventPlayerLoginSuccess(final @NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
