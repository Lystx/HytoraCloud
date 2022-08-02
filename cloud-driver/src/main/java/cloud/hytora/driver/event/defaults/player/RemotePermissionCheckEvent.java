package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@RequiredArgsConstructor
public class RemotePermissionCheckEvent implements CloudEvent {

    private final ICloudPlayer player;
    private final String permission;

    @Setter
    private boolean hasPermission;
}
