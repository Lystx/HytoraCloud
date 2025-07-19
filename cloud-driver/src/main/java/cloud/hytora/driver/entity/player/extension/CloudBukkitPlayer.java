package cloud.hytora.driver.entity.player.extension;

import cloud.hytora.common.location.impl.CloudLocation;

public interface CloudBukkitPlayer  {

    void sendMessage(String message);

    CloudLocation getLocation();

    void teleport(CloudLocation location);
}
