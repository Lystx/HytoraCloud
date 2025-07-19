package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.event.defaults.CancelableEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
@Getter
public class CloudEventPlayerLogin extends CancelableEvent {


    /**
     * The connection that is trying to log in
     */
    private final PlayerConnection connection;

    /**
     * The server that player will first connect to
     */
    private CloudService firstJoinServer;

}
