package cloud.hytora.driver.entity.player.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerFullJoinExecutor;


public class DefaultFullJoinExecutor implements PlayerFullJoinExecutor {


    @Override
    public Result execute(CloudPlayer cloudPlayer, boolean sentToHub, boolean disconnect) {

        boolean kickPlayersThatAreNotOnFallback = CloudDriver.getInstance().getConfigManager().getConfig().isKickPlayersNotOnFallback();
        Checker provider = CloudDriver.getInstance().getProvider(Checker.class);
        if (provider == null) {
            return new Result(ResultType.NOT_PERFORMED, -1);
        }
        int kickedPlayers = 0;
        for (CloudPlayer onlinePlayer : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            if ((kickPlayersThatAreNotOnFallback && (onlinePlayer.getServer() == null || onlinePlayer.getServer().getTask().getFallback().isEnabled())) || provider.compare(cloudPlayer, onlinePlayer).equals(cloudPlayer)) {
                if (sentToHub) {
                    onlinePlayer.asProxyPlayer().sendToFallback();
                }
                if (disconnect) {
                    onlinePlayer.asProxyPlayer().disconnect(CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getHigherPriorityJoined().replace("%prefix%", CloudDriver.getInstance().getConfigManager().getConfig().getMessages().getPrefix()));
                }
                kickedPlayers += 1;
            }
        }


        if (kickedPlayers > 0) {
            return new Result(ResultType.ALLOWED, kickedPlayers);
        } else {
            return new Result(ResultType.NO_LOWER_PLAYERS_THAN_SELF, kickedPlayers);
        }

    }
}
