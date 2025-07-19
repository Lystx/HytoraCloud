package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.entity.player.CloudPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class CloudEventPlayerPermissionCheck implements LocalEvent {

    private final CloudPlayer player;
    private final String permission;

    @Setter
    private boolean hasPermission;
}
