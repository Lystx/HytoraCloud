package cloud.hytora.driver.player;

import cloud.hytora.common.task.Task;

public interface PlayerFullJoinExecutor {

    Task<Void> execute(ICloudPlayer cloudPlayer, boolean sentToHub , boolean disconnect, boolean kickPlayersOnFallbackIfLowerRank);
}
