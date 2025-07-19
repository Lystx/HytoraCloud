package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.event.LocalEvent;
import cloud.hytora.driver.entity.player.CloudPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
public class CloudEventPlayerCommand implements LocalEvent {

    private final CloudPlayer cloudPlayer;
    private final String message;

    @Setter
    private boolean cancelled;

}
