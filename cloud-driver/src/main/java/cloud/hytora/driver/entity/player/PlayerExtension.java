package cloud.hytora.driver.entity.player;

import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;

public interface PlayerExtension {

    CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer);

    CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer);
}
