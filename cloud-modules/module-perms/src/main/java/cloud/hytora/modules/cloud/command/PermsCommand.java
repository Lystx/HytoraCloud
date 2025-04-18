package cloud.hytora.modules.cloud.command;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.modules.cloud.setup.GroupSetup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command(
        value = {"perms", "perm"},
        permission = "cloud.module.perms.command.use",
        description = "Manages the perms-module",
        executionScope = CommandScope.CONSOLE_AND_INGAME
)
@Command.AutoHelp
public class PermsCommand {


    @Command("user")
    @Command.Syntax("<player> info")
    public void onPlayerInfo(CommandSender sender, @Command.Argument("player") PermissionPlayer player) {

        if (player == null) {
            sender.sendMessage("§cThere is no such player registered in the modules' database!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("Player information:");
        sender.sendMessage("§bName: §7" + player.getName() + " §8[§3" + player.getUniqueId() + "§8]");
        sender.sendMessage("§bHighest Group: §7" + (player.getHighestGroup() == null ? "none" : player.getHighestGroup().getName()));
        sender.sendMessage("§bAll Groups: §7" + player.getPermissionGroups().stream().map(PermissionGroup::getName).collect(Collectors.joining(", ")));
        sender.sendMessage("§bPermissions: §7" + player.getPermissions().size());
        sender.sendMessage("§8");
    }

    @Command("debug")
    @Command.Syntax("<server>")
    public void onDebug(CommandSender sender, @Command.Argument("server") ICloudService service) {

        if (service == null) {
            sender.sendMessage("§cThere is no such service online!");
            return;
        }


        service.sendDocument(
                new PermsCacheUpdatePacket(
                        CloudDriver.getInstance()
                                .getProviderRegistry()
                                .getUnchecked(PermissionManager.class)
                                .getAllCachedPermissionGroups()
                )
        );
        sender.sendMessage("§aDone! Debugged");
    }

    @Command("user")
    @Command.Syntax("<player> createManually")
    public void onPlayerCreate(CommandSender sender, @Command.Argument("player") CloudOfflinePlayer player) {
        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }

        PermissionPlayer pp = new DefaultPermissionPlayer(player.getName(), player.getUniqueId());
        pp.update();
        sender.sendMessage("Successfully created User[name={}, uuid={}]", pp.getName(), pp.getUniqueId());
    }

    @Command("user")
    @Command.Syntax("<player> group add <group>")
    public void onPlayerGroupAdd(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("group") PermissionGroup group) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            sender.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        player.addPermissionGroup(group);
        player.update();
        sender.sendMessage("Successfully added {} to group {} [PERMANENT]", player.getName(), group.getName());
    }

    @Command("user")
    @Command.Syntax("<player> group addTemp <group> <time> <unit>")
    public void onPlayerGroupAddTemporary(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("group") PermissionGroup group, @Command.Argument("time") long time, @Command.Argument("unit") String unit) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            sender.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        if (time <= 0) {
            sender.sendMessage("§cPlease provide a time that is bigger than 0!");
        }

        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            player.addPermissionGroup(group, timeUnit, time);
            player.update();
            sender.sendMessage("Successfully added {} to group {} [{} {}]", player.getName(), group.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command("user")
    @Command.Syntax("<player> group remove <group>")
    public void onPlayerGroupRemove(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("group") PermissionGroup group) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            sender.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        if (!player.isInPermissionGroup(group.getName())) {
            sender.sendMessage("§cThe player does not have the provided PermissionGroup!");
            return;
        }

        player.removePermissionGroup(group.getName());
        player.update();
        sender.sendMessage("Successfully removed {} from group {}!", player.getName(), group.getName());
    }

    @Command("user")
    @Command.Syntax("<player> perms add <permission>")
    public void onPlayerPermissionAdd(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (player.hasPermission(permission)) {
            sender.sendMessage("§cThe player already has this permission!");
            return;
        }

        player.addPermission(Permission.of(permission));
        player.update();

        sender.sendMessage("Successfully added permission {} to player {} [PERMANENT]", permission, player.getName());
    }

    @Command("user")
    @Command.Syntax("<player> perms addTemp <permission> <time> <unit>")
    public void onPlayerPermissionAddTemporary(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("permission") String permission, @Command.Argument("time") long time, @Command.Argument("unit") String unit) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }

        if (player.hasPermission(permission)) {
            sender.sendMessage("§cThe player already has this permission!");
            return;
        }

        if (time <= 0) {
            sender.sendMessage("§cPlease provide a time that is bigger than 0!");
        }

        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            player.addPermission(Permission.of(permission, timeUnit, time));
            player.update();
            sender.sendMessage("Successfully added permission {} to player {} [{} {}]", permission, player.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command("user")
    @Command.Syntax("<player> perms remove <permission>")
    public void onPlayerPermissionRemove(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }


        if (!player.hasPermission(permission)) {
            sender.sendMessage("§cThe player does not have this permission!");
            return;
        }


        player.removePermission(Permission.of(permission));
        player.update();
        sender.sendMessage("Successfully removed permission {} from player {}!", permission, player.getName());
    }


    @Command(value = "user", description = "Adds the provided permission for a given task to a player" )
    @Command.Syntax("<player> perms addTask <taskName> <permission>")
    public void onPlayerTaskPermissionAdd(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("taskName") IServiceTask task, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (task == null) {
            sender.sendMessage("§cThere is no such task!");
            return;
        }

        if (task.getVersion().isProxy()) {
            sender.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (player.getTaskPermissions(task.getName()).contains(permission)) {
            sender.sendMessage("§cThe player already has this taskPermission!");
            return;
        }

        player.addTaskPermission(task, permission);
        player.update();
        sender.sendMessage("Successfully added permission {} to player {} for task {}!", permission, player.getName(), task.getName());
    }

    @Command(value = "user", description = "Removes the provided permission for a given task from a player")
    @Command.Syntax("<player> perms removeTask <taskName> <permission>")
    public void onPlayerTaskPermissionRemove(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("taskName") IServiceTask task, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (task.getVersion().isProxy()) {
            sender.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }


        if (!player.getTaskPermissions(task.getName()).contains(permission)) {
            sender.sendMessage("§cThe player does not have this taskPermission!");
            return;
        }

        player.removeTaskPermission(task, permission);
        player.update();
        sender.sendMessage("Successfully removed permission {} from player {} for task {}!", permission, player.getName(), task.getName());
    }


    @Command(value = "user", description = "Removes the provided permission from the DeniedPermissions-List")
    @Command.Syntax("<player> perms allowPermission <permission>")
    public void onPlayerPermissionAllow(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThere is no such player in the module database registered");
            return;
        }
        if (!player.getDeniedPermissions().contains(permission)) {
            sender.sendMessage("§cThe player is already forbidden to have this permission!");
            return;
        }

        player.removeDeniedPermission(permission);
        player.update();
        sender.sendMessage("Successfully allowed permission {} for player {} !", permission, player.getName());
    }

    @Command(value = "user", description = "Denies the provided permission for a player")
    @Command.Syntax("<player> perms denyPermission <permission>")
    public void onPlayerPermissionDeny(CommandSender sender, @Command.Argument("player") PermissionPlayer player, @Command.Argument("permission") String permission) {

        if (player == null) {
            sender.sendMessage("§cThere is no such player in the module database registered");
            return;
        }
        if (player.getDeniedPermissions().contains(permission)) {
            sender.sendMessage("§cThe player is already forbidden to have this permission!");
            return;
        }

        player.addDeniedPermission(permission);
        player.update();
        sender.sendMessage("Successfully denied permission {} for player {} !", permission, player.getName());
    }


    @Command("group")
    @Command.Syntax("<group> info")
    public void onGroupInfo(CommandSender sender, @Command.Argument("group") PermissionGroup group) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group registered in the module database!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("Group information:");
        sender.sendMessage("§bName: §7" + group.getName());
        sender.sendMessage("§bSortId: §7" + group.getSortId());
        sender.sendMessage("§bDefaultGroup: §7" + group.isDefaultGroup());
        sender.sendMessage("§bChatColor: §7" + group.getChatColor());
        sender.sendMessage("§bNamePrefix: §7" + group.getNamePrefix());
        sender.sendMessage("§bPrefix: §7" + group.getPrefix());
        sender.sendMessage("§bSuffix: §7" + group.getSuffix());
        sender.sendMessage("§bInherited Groups: §7" + String.join(", ", group.getInheritedGroups()));
        sender.sendMessage("§bPermissions: §7" + group.getPermissions().size());
        sender.sendMessage("§8");
    }

    @Command(value = "group", description = "Creates a new Group")
    @Command.Syntax("create")
    public void onGroupCreate(CommandSender sender) {
        PermissionManager permissionManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);
        new GroupSetup().start((setup, state) -> {
            switch (state) {
                case CANCELLED:
                    sender.sendMessage("§cNo group was created!");
                    break;

                case FINISHED:

                    String name = setup.getName();
                    int sortId = setup.getSortId();

                    PermissionGroup permissionGroup = permissionManager.createPermissionGroup(name);

                    String[] split = setup.getInheritedGroups().split(",");

                    permissionGroup.setName(name);
                    permissionGroup.setSortId(sortId);
                    permissionGroup.setDefaultGroup(setup.isDefaultGroup());
                    permissionGroup.setPrefix(setup.getPrefix());
                    permissionGroup.setSuffix(setup.getSuffix());
                    permissionGroup.setChatColor(setup.getChatColor());

                    for (String s : split) {
                        permissionGroup.addInheritedGroup(s);
                    }

                    permissionGroup.update();

                    sender.sendMessage("Successfully created PermissionGroup[name={}, id={}]", name, sortId);
                    break;
            }
        });
    }


    @Command("group")
    @Command.Syntax("<group> delete")
    public void onGroupDelete(CommandSender sender, @Command.Argument("group") PermissionGroup group) {
        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).deletePermissionGroup(group.getName());
        sender.sendMessage("Successfully deleted group {}", group.getName());
    }


    @Command("group")
    @Command.Syntax("<group> perms add <permission>")
    public void onGroupPermissionAdd(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.hasPermission(permission)) {
            sender.sendMessage("§cThe group already has this permission!");
            return;
        }

        group.addPermission(Permission.of(permission));
        group.update();

        sender.sendMessage("Successfully added permission {} to group {} [PERMANENT]", permission, group.getName());
    }

    @Command("group")
    @Command.Syntax("<group> perms addTemp <permission> <time> <unit>")
    public void onPlayerPermissionAddTemporary(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("permission") String permission, @Command.Argument("time") long time, @Command.Argument("unit") String unit) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.hasPermission(permission)) {
            sender.sendMessage("§cThe group already has this permission!");
            return;
        }

        if (time <= 0) {
            sender.sendMessage("§cPlease provide a time that is bigger than 0!");
        }
        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            group.addPermission(Permission.of(permission, timeUnit, time));
            group.update();
            sender.sendMessage("Successfully added permission {} to group {} [{} {}]", permission, group.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command(value = "group", description = "Adds the provided permission for a given task to a group")
    @Command.Syntax("<group> perms addTask <taskName> <permission>")
    public void onGroupTaskPermissionAdd(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("taskName") IServiceTask task, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        if (task.getVersion().isProxy()) {
            sender.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (group.getTaskPermissions(task.getName()).contains(permission)) {
            sender.sendMessage("§cThe group already has this taskPermission!");
            return;
        }

        group.addTaskPermission(task, permission);
        group.update();
        sender.sendMessage("Successfully added permission {} to group {} for task {}!", permission, group.getName(), task.getName());
    }

    @Command(value = "group", description = "Removes the provided permission for a given task from a group")
    @Command.Syntax("<group> perms removeTask <taskName> <permission>")
    public void onGroupTaskPermissionRemove(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("taskName") IServiceTask task, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        if (task.getVersion().isProxy()) {
            sender.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (!group.getTaskPermissions(task.getName()).contains(permission)) {
            sender.sendMessage("§cThe group does not have this taskPermission!");
            return;
        }

        group.removeTaskPermission(task, permission);
        group.update();
        sender.sendMessage("Successfully removed permission {} from group {} for task {}!", permission, group.getName(), task.getName());
    }


    @Command(value = "group", description = "Removes the provided permission from the DeniedPermissions-List")
    @Command.Syntax("<group> perms allowPermission <permission>")
    public void onGroupPermissionAllow(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (!group.getDeniedPermissions().contains(permission)) {
            sender.sendMessage("§cThe group is allowed to have this permission!");
            return;
        }

        group.removeDeniedPermission(permission);
        group.update();
        sender.sendMessage("Successfully allowed permission {} for group {} !", permission, group.getName());
    }

    @Command(value = "group", description = "Denies the provided permission for a group")
    @Command.Syntax("<group> perms denyPermission <permission>")
    public void onGroupPermissionDeny(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.getDeniedPermissions().contains(permission)) {
            sender.sendMessage("§cThe group is already forbidden to have this permission!");
            return;
        }

        group.addDeniedPermission(permission);
        group.update();
        sender.sendMessage("Successfully denied permission {} for group {} !", permission, group.getName());
    }

    @Command(value = "group", description = "Removes the provided permission from a group")
    @Command.Syntax("<group> perms remove <permission>")
    public void onGroupPermissionRemove(CommandSender sender, @Command.Argument("group") PermissionGroup group, @Command.Argument("permission") String permission) {

        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (!group.hasPermission(permission)) {
            sender.sendMessage("§cThe group does not have this permission!");
            return;
        }

        group.removePermission(Permission.of(permission));
        group.update();
        sender.sendMessage("Successfully removed permission {} from group {}!", permission, group.getName());
    }

}
