package cloud.hytora.driver.player;

import cloud.hytora.common.task.ITask;

public interface PlayerFullJoinExecutor {

    ITask<Void> execute(ICloudPlayer cloudPlayer, boolean sentToHub , boolean disconnect, boolean kickPlayersOnFallbackIfLowerRank);
}
