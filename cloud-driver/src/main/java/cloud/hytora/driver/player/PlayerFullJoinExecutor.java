package cloud.hytora.driver.player;

import cloud.hytora.common.task.IPromise;

public interface PlayerFullJoinExecutor {

    IPromise<Void> execute(ICloudPlayer cloudPlayer, boolean sentToHub , boolean disconnect, boolean kickPlayersOnFallbackIfLowerRank);
}
