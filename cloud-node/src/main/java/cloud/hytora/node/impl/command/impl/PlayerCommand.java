package cloud.hytora.node.impl.command.impl;


import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.PagedList;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.entity.player.CloudOfflinePlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerManager;
import cloud.hytora.driver.entity.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.entity.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import cloud.hytora.node.NodeDriver;

import java.text.SimpleDateFormat;
import java.util.*;

@Command(
        value = {"players", "player"},
        permission = "cloud.hytora.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all players"

)
@Command.AutoHelp

public class PlayerCommand {


    @Command(value = "list", description = "Lists all players")
    public void executeList(CommandSender sender) {

        Collection<CloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("§cThere are currently no players online!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Players (" + players.size() + ")§8:");

        for (CloudPlayer player : players) {
            sender.sendMessage("%1" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        sender.sendMessage("§8");
    }

    private boolean loadedCacheOnce = false;

    @Command(value = "listOffline", description = "Lists all saved players")
    @Command.Syntax("<page>")
    public void executeListOffline(CommandSender sender, @Command.Argument("page") int page) {


        sender.sendMessage("§7Loading %1ALL SAVED §7OfflinePlayers§8...");
        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();
        Task<Collection<CloudOfflinePlayer>> task;
        if (!loadedCacheOnce) {
            task = playerManager.getOfflinePlayers();
            loadedCacheOnce = true;
        } else {
            task = Task.build(playerManager.getCachedOfflinePlayers());
        }
        task.onTaskSucess(result -> {
            PagedList<CloudOfflinePlayer> offlinePlayers = CollectionUtils.paged(new ArrayList<>(result), 15, page);
            if (offlinePlayers.isEmpty()) {
                sender.sendMessage("§cThere are no OfflinePlayers stored on page §e{}", offlinePlayers.getCurrentPage());
                return;
            }
            sender.sendMessage("§7Loaded %1{} §7OfflinePlayers §8[%2Page§8: %2{}§8/%2{}§8]", offlinePlayers.size(), offlinePlayers.getCurrentPage(), offlinePlayers.getMaxPages());

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");
            for (CloudOfflinePlayer offlinePlayer : offlinePlayers) {

                sender.sendMessage("  §8» %1{}", offlinePlayer.getName());
                sender.sendMessage("     §8» %1UUID: §7" + (offlinePlayer.getUniqueId()));
                sender.sendMessage("     §8» %1First Login: §7" + sdf.format(new Date(offlinePlayer.getFirstLogin())));
                sender.sendMessage("     §8» %1Last Login: §7" + sdf.format(new Date(offlinePlayer.getLastLogin())));
                sender.sendMessage("     §8» %1Status: §7" + (offlinePlayer.isOnline() ? "§aOnline" : "§cOffline"));
            }
            sender.sendMessage("§7Next Page§8: %2{}", (offlinePlayers.getCurrentPage() + 1));
        }).onTaskFailed(e -> sender.sendMessage("§cSomething went wrong whilst loading all saved OfflinePlayers!"));

    }

    @Command(value = "dummy", description = "Creates dummy player")
    @Command.Syntax("<name> <uuid>")
    public void handleDummy(CommandSender sender, @Command.Argument("name") String name, @Command.Argument("uuid") UUID uuid) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();

        if (playerManager.getOfflinePlayer(name).syncUninterruptedly().get() != null) {
            sender.sendMessage("§cThis player already exists in database");
            return;
        }

        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);
        PermissionPlayer permissionPlayer = permissionManager.getPermissionPlayer(name);
        if (permissionPlayer == null) {
            PermissionPlayer player = permissionManager.createPlayer(name, uuid);
            player.update();
        }
        PermissionGroup permissionGroup = permissionManager.getAllCachedPermissionGroups().stream().filter(PermissionGroup::isDefaultGroup).findFirst().orElse(null);

        DefaultCloudOfflinePlayer player = new DefaultCloudOfflinePlayer(uuid, name, System.currentTimeMillis(), System.currentTimeMillis());
        player.setProperty("module_perms_highest_group", permissionGroup.getName());

        player.save();
        sender.sendMessage("§7Created DummyPlayer %1" + player.getName());
    }

    @Command(value = "simulate", description = "Simulates a player executing a command")
    @Command.Syntax("<type> <name> <command>")
    public void handleSimulation(
            CommandSender sender,
            @Command.Argument("type") String type,
            @Command.Argument("name") String name,
            @Command.Argument("command") String cmd
    ) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();

        CloudOfflinePlayer player = playerManager.getOfflinePlayer(name).syncUninterruptedly().get();
        if (player == null) {
            sender.sendMessage("§cThis player does not exists in database");
            return;
        }
        if (playerManager.getCachedCloudPlayer(name) != null) {
            sender.sendMessage("§cCan't simulate command for online player!");
            return;
        }


        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);

        PermissionPlayer permissionPlayer = permissionManager.getPermissionPlayer(name);
        if (permissionPlayer == null) {
            PermissionPlayer pp = permissionManager.createPlayer(player.getName(), player.getUniqueId());
            pp.update();
        }

        try {
            String[] command = cmd.split("_");
            CommandScope commandScope = CommandScope.valueOf(type);
            CloudService firstProxy = CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY).stream().findFirst().orElse(null);
            CloudService firstServer = CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
                    .filter(service -> service.getServiceState() == ServiceState.ONLINE)
                    .filter(service -> service.getServiceVisibility() == ServiceVisibility.VISIBLE)
                    .filter(service -> !service.getTask().getVersion().isProxy())
                    .filter(service -> service.getTask().getFallback().isEnabled())
                    .min(Comparator.comparing(s -> s.getOnlinePlayers().size()))
                    .orElse(null);

            if (firstProxy == null || firstServer == null) {
                sender.sendMessage("§cNo suitable Proxy or Server found!");
                sender.sendMessage("§cPlease ensure that atleast one Proxy- and Minecraftserver is online!");
                return;
            }
            sender.sendMessage("Simulating that §8'%1{}§8' §fexecutes Command §8'§e{}§8' §8[%2Type§8: %1{}§8]", player.getName(), Arrays.toString(command), type);

            UniversalCloudPlayer cloudPlayer = new UniversalCloudPlayer(
                    player.getUniqueId(),
                    player.getName(),
                    player.getFirstLogin(),
                    player.getLastLogin(),
                    firstServer,
                    firstProxy,
                    player.getProperties()
            );

            cloudPlayer.update();


            StringBuilder buildCommand = new StringBuilder();
            for (String s : command) {
                buildCommand.append(" ").append(s);
            }

            if (commandScope == CommandScope.INGAME) {

                PacketCloudEntityPlayer commandPacket = PacketCloudEntityPlayer.forPlayerCommandExecute(cloudPlayer.getUniqueId(), buildCommand.toString());
                commandPacket.sendQuery()
                        .setReceivers(firstServer.getName())
                        .execute()
                        .onTaskSucess(response -> {
                            List<String> outputs = new ArrayList<>();
                            sender.sendMessage(" §8 ");
                            sender.sendMessage("§7The §6Command-Execution §7brought back following §eCommand-Output§8:");

                            //after command unregister cloudPlayer
                            CloudDriver.getInstance().getPlayerManager().unregisterCloudPlayer(cloudPlayer.getUniqueId(), cloudPlayer.getName());


                        });
            } else {
                sender.sendMessage("§cOther scopes are not possible at the moment");
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cPossible CommandTypes: §e" + Arrays.toString(CommandScope.values()));
        }
    }

    @Command(value = "kick", description = "Kicks a player")
    @Command.Syntax("<name>")
    public void executeKick(CommandSender sender, @Command.Argument("name") String name) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();

        CloudPlayer cloudPlayer = playerManager.getCachedCloudPlayer(name);
        if (cloudPlayer == null) {
            sender.sendMessage("§cDieser Spieler ist nicht auf dem Netzwerk!");
            return;
        }

        cloudPlayer.asProxyPlayer().disconnect("§cYou got kicked by the CloudSystem!");
        sender.sendMessage("§7Kicking §8'{}§8'§8...", name);
    }

    @Command(value = "forceKick", description = "Forces a player to be kicked and also be force-unregistered from cache")
    @Command.Syntax("<name>")
    public void executeForceKick(CommandSender sender, @Command.Argument("name") String name) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();

        CloudPlayer cloudPlayer = playerManager.getCachedCloudPlayer(name);
        if (cloudPlayer == null) {
            sender.sendMessage("§cDieser Spieler ist nicht auf dem Netzwerk!");
            return;
        }

        cloudPlayer.asProxyPlayer().disconnect("§cYou got kicked by the CloudSystem!");
        playerManager.unregisterCloudPlayer(cloudPlayer.getUniqueId(), cloudPlayer.getName());
        sender.sendMessage("§7Force kicking §8'{}§8'§8...", name);
        sender.sendMessage(playerManager.getCachedCloudPlayer(cloudPlayer.getUniqueId()) == null ? "  §8=> §aKicked!" : "  §8=> §cSomething went wrong");
        PacketDriverCacheUpdate.publishUpdate(NodeDriver.getInstance().getExecutor());
    }

    @Command(value = "info", description = "Shows information about a player")
    @Command.Syntax("<name>")
    public void executeInfo(CommandSender sender, @Command.Argument("name") String name) {

        PlayerManager playerManager = CloudDriver.getInstance().getPlayerManager();
        playerManager.getOfflinePlayer(name)
                .onTaskSucess(player -> {
                    if (player == null) {
                        sender.sendMessage("§cNo such player with the name §e" + name + " §chas ever joined the network!");
                        return;
                    }

                    SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy - HH:mm:ss");

                    sender.sendMessage("§8");
                    sender.sendMessage("Player information:");
                    sender.sendMessage("  §8» %1Name: §7" + player.getName() + " §8[%2" + player.getUniqueId() + "§8]");
                    sender.sendMessage("  §8» %1First Login: §7" + sdf.format(new Date(player.getFirstLogin())));
                    sender.sendMessage("  §8» %1Last Login: §7" + sdf.format(new Date(player.getLastLogin())));
                    sender.sendMessage("  §8» %1Properties: §7" + player.getProperties().asRawJsonString());
                    sender.sendMessage("  §8» %1Status: §7" + (player.isOnline() ? "§aOnline" : "§cOffline"));
                    if (player.isOnline()) {
                        CloudPlayer onlinePlayer = player.asOnlinePlayer();
                        sender.sendMessage("  §8» %1Proxy: §7" + onlinePlayer.getProxyServer());
                        sender.sendMessage("  §8» %1Server: §7" + onlinePlayer.getServer());

                    }
                    if (!player.getProperties().has("debugged")) {

                        player.setProperty("debugged", true);
                        player.save();
                    }
                    sender.sendMessage("§8");
                }).onTaskFailed(e -> {
                });
    }
}
   