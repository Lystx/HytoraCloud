package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.entity.player.CloudPlayer;
import org.jetbrains.annotations.NotNull;

public class CloudEventPlayerDisconnect extends DefaultPlayerEvent  {

    public CloudEventPlayerDisconnect() {
        super();
    }

    public CloudEventPlayerDisconnect(@NotNull CloudPlayer cloudPlayer) {
        super(cloudPlayer);
    }

}
