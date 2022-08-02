package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.player.ICloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudPlayerDisconnectEvent extends DefaultPlayerEvent {

    public CloudPlayerDisconnectEvent(@NotNull ICloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
