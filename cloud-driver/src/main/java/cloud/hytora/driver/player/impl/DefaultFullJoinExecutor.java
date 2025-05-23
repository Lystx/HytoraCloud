package cloud.hytora.driver.player.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.CloudException;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerFullJoinChecker;
import cloud.hytora.driver.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.player.executor.PlayerExecutor;

public class DefaultFullJoinExecutor implements PlayerFullJoinExecutor {


    @Override
    public Task<Void> execute(ICloudPlayer cloudPlayer, boolean sentToHub, boolean disconnect) {

        Task<Void> task = Task.empty();
        boolean kickPlayersThatAreNotOnFallback = false; // TODO: 02.08.2022 custom config
        CloudDriver.getInstance().get(PlayerFullJoinChecker.class).ifPresent(playerFullJoinExecutor -> {
            int kickedPlayers = 0;
            for (ICloudPlayer onlinePlayer : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
                if ((kickPlayersThatAreNotOnFallback && (onlinePlayer.getServer() == null || onlinePlayer.getServer().getTask().getFallback().isEnabled())) || playerFullJoinExecutor.compare(cloudPlayer, onlinePlayer).equals(cloudPlayer)) {
                    PlayerExecutor playerExecutor = PlayerExecutor.forPlayer(onlinePlayer);
                    if (sentToHub) {
                        playerExecutor.sendToFallback();
                    }
                    if (disconnect) {
                        playerExecutor.disconnect("§cA player with a higher priority joined");
                    }
                    kickedPlayers += 1;
                }
            }


            if (kickedPlayers > 0) {
                task.setResult(null);
            } else {
                task.setFailure(new CloudException("No player with lower priority than self"));
            }
        });

        return task;
    }
}
