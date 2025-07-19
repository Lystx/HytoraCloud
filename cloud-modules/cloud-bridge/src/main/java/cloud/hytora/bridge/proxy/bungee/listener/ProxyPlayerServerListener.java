package cloud.hytora.bridge.proxy.bungee.listener;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.config.def.UniversalCloudMessages;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.entity.player.connection.ProtocolVersion;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.entity.services.ServiceManager;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.*;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class ProxyPlayerServerListener implements Listener {


    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(ServerConnectEvent event) {
        ServerInfo target = event.getTarget();
        ProxiedPlayer player = event.getPlayer();

        boolean join = false;
        //Player is joining the network
        if (player.getServer() == null) {
            join = true;
        }
        UniversalCloudMessages cloudMessages = CloudDriver.getInstance().getConfigManager().getConfig().getMessages();

        ServiceManager serviceManager = CloudDriver.getInstance().getServiceManager();
        CloudService server = serviceManager.getCachedCloudService(target.getName());
        CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());

        if (cloudPlayer == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": Your UUID is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }
        if (server == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Service is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }
        ServiceTask task = server.getTask();

        if (task == null) {
            player.disconnect(cloudMessages.getPrefix() + " §cAn error occured whilst trying to connect you to " + target.getName() + ": This Task is not registered in CloudCache!");
            event.setCancelled(true);
            return;
        }
        if (task.isMaintenance() && !player.hasPermission("cloud.maintenance.bypass")) {
            player.sendMessage(cloudMessages.getPrefix() + " §cThis ServiceTask is currently in §emaintenance§c!");
            event.setCancelled(true);
            return;
        }

        ProtocolVersion.SwitchResult switchResult = server.canJoin(cloudPlayer);
        if (!switchResult.isAllowed()) {
            if (join) {
                player.disconnect(cloudMessages.getPrefix() + cloudMessages.getHigherMinecraftVersionNeeded().replaceAll("%version%", switchResult.getServerVersion().getMainName()).replaceAll("%player_version%", switchResult.getPlayerVersion().getMainName()));
            } else {
                player.sendMessage(cloudMessages.getPrefix() + cloudMessages.getHigherMinecraftVersionNeeded().replaceAll("%version%", switchResult.getServerVersion().getMainName()).replaceAll("%player_version%", switchResult.getPlayerVersion().getMainName()));
            }
            event.setCancelled(true);
            return;
        }

        boolean hasTaskPermission = cloudPlayer.hasPermission(task.getPermission());

        if ((task.getPermission() != null && !task.getPermission().trim().isEmpty() && !hasTaskPermission)) {
            //kick player
            event.setCancelled(true);
            player.sendMessage(StringUtils.formatMessage(cloudMessages.getPrefix() +cloudMessages.getTaskHasPermissionMessage(), server.getTask().getPermission()));
            return;
        }



        if (server.isFull()) {
            PlayerFullJoinExecutor fullJoinExecutor = CloudDriver.getInstance().getProvider(PlayerFullJoinExecutor.class);
            PlayerFullJoinExecutor.Result result = fullJoinExecutor.execute(cloudPlayer, (!join), (join));
            if (result.getType() == PlayerFullJoinExecutor.ResultType.NO_LOWER_PLAYERS_THAN_SELF) {
                player.sendMessage(cloudMessages.getPrefix() + cloudMessages.getNoAvailableFallbackMessage());
                event.setCancelled(true);
                return;
            }
        }

        event.setCancelled(false);

        PacketCloudEntityPlayer.forServerConnected(cloudPlayer.getUniqueId(), server.getName()).publish();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void handle(ServerConnectedEvent event) {
        Server server = event.getServer();
        ProxiedPlayer player = event.getPlayer();

        PacketCloudEntityPlayer.forServerConnectedSuccess(player.getUniqueId(), server.getInfo().getName()).publish();
    }

}
