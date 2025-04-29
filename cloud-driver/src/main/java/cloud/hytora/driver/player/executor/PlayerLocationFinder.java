package cloud.hytora.driver.player.executor;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import cloud.hytora.driver.player.ICloudPlayer;

public interface PlayerLocationFinder {


    CloudEntityLocation<Double, Float> getLocation(ICloudPlayer player);
}
