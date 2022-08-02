package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import lombok.AllArgsConstructor;

import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class DefaultPlayerEvent implements CloudEvent {

    private ICloudPlayer cloudPlayer;

}
