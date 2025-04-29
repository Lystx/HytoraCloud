package cloud.hytora.node.impl.command.impl;

import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerManager;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.driver.player.impl.UniversalCloudPlayer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;

import java.text.SimpleDateFormat;
import java.util.*;

@Command(
        value = {"players", "player"},
        permission = "cloud.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all players"

)
@Command.AutoHelp
@ApplicationParticipant
public class PlayerCommand {


    @Command(value = "list", description = "Lists all players")
    public void executeList(CommandSender sender) {

        Collection<ICloudPlayer> players = CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers();

        if (players.isEmpty()) {
            sender.sendMessage("§cThere are currently no players online!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§7Players (" + players.size() + ")§8:");

        for (ICloudPlayer player : players) {
            sender.sendMessage("§b" + player.getName() + " §8[§e" + player.getProxyServer() + " | " + player.getServer() + "§8]");
        }
        sender.sendMessage("§8");
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
        permissionManager.getPlayerAsyncByName(name)
                .onTaskSucess(p -> {
                   if (p == null) {
                       PermissionPlayer player = permissionManager.createPlayer(name, uuid);
                       player.update();
                   }
                });

        PermissionGroup permissionGroup = permissionManager.getAllCachedPermissionGroups().stream().filter(PermissionGroup::isDefaultGroup).findFirst().orElse(null);

        DefaultCloudOfflinePlayer player = new DefaultCloudOfflinePlayer(uuid, name, System.currentTimeMillis(), System.currentTimeMillis(), Document.newJsonDocument().set("module_perms_highest_group", permissionGroup.getName()));

        player.saveOfflinePlayer();
        sender.sendMessage("§7Created DummyPlayer §b" + player.getName());
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
        permissionManager.getPlayerAsyncByName(player.getName())
                .onTaskSucess(p -> {
                    if (p == null) {
                        PermissionPlayer pp = permissionManager.createPlayer(player.getName(), player.getUniqueId());
                        pp.update();
                    }
                });

        try {
            String[] command = cmd.split("_");
            CommandScope commandScope = CommandScope.valueOf(type);
            ICloudService firstProxy = CloudDriver.getInstance().getServiceManager().getAllServicesByEnvironment(SpecificDriverEnvironment.PROXY).stream().findFirst().orElse(null);
            ICloudService firstServer = CloudDriver.getInstance().getServiceManager().getAllCachedServices().stream()
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
            sender.sendMessage("Simulating that §8'§b{}§8' §fexecutes Command §8'§e{}§8' §8[§3Type§8: §b{}§8]", player.getName(), Arrays.toString(command), type);

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

                PacketCloudPlayer commandPacket = PacketCloudPlayer.forPlayerCommandExecute(cloudPlayer.getUniqueId(), buildCommand.toString());
                commandPacket.awaitResponse(firstServer.getName()).onTaskSucess(response -> {
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
                    sender.sendMessage("Service information:");
                    sender.sendMessage("§bName: §7" + player.getName() + " §8[§3" + player.getUniqueId() + "§8]");
                    sender.sendMessage("§bFirst Login: §7" + sdf.format(new Date(player.getFirstLogin())));
                    sender.sendMessage("§bLast Login: §7" + sdf.format(new Date(player.getLastLogin())));
                    sender.sendMessage("§bProperties: §7" + player.getProperties().asRawJsonString());
                    sender.sendMessage("§bStatus: §7" + (player.isOnline() ? "§aOnline" : "§cOffline"));
                    if (player.isOnline()) {
                        ICloudPlayer onlinePlayer = player.asOnlinePlayer();
                        sender.sendMessage("§bProxy: §7" + onlinePlayer.getProxyServer());
                        sender.sendMessage("§bServer: §7" + onlinePlayer.getServer());

                    }
                    if (!player.getProperties().has("debugged")) {
                        player.editProperties(properties -> {
                            properties.set("debugged", true);
                        });
                    }
                    sender.sendMessage("§8");
                }).onTaskFailed(e -> {
                });

    }
}
