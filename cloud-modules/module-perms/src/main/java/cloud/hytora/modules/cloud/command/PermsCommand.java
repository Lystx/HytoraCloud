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
import cloud.hytora.modules.cloud.setup.GroupSetup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandDescription("Manages the perms-module")
@Command({"perms", "perm"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.module.perms.command.use")
@CommandAutoHelp
public class PermsCommand {


    @Command("user")
    @Syntax("<player> info")
    public void onPlayerInfo(CommandSender sender, @Argument("player") PermissionPlayer player) {

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

    @Command("user")
    @Syntax("<player> createManually")
    public void onPlayerCreate(CommandSender sender, @Argument("player") CloudOfflinePlayer player) {
        if (player == null) {
            sender.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }

        PermissionPlayer pp = new DefaultPermissionPlayer(player.getName(), player.getUniqueId());
        pp.update();
        sender.sendMessage("Successfully created User[name={}, uuid={}]", pp.getName(), pp.getUniqueId());
    }

    @Command("user")
    @Syntax("<player> group add <group>")
    public void onPlayerGroupAdd(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("group") PermissionGroup group) {

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
    @Syntax("<player> group addTemp <group> <time> <unit>")
    public void onPlayerGroupAddTemporary(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("group") PermissionGroup group, @Argument("time") long time, @Argument("unit") String unit) {

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
    @Syntax("<player> group remove <group>")
    public void onPlayerGroupRemove(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("group") PermissionGroup group) {

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
    @Syntax("<player> perms add <permission>")
    public void onPlayerPermissionAdd(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("permission") String permission) {

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
    @Syntax("<player> perms addTemp <permission> <time> <unit>")
    public void onPlayerPermissionAddTemporary(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("permission") String permission, @Argument("time") long time, @Argument("unit") String unit) {

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
    @Syntax("<player> perms remove <permission>")
    public void onPlayerPermissionRemove(CommandSender sender, @Argument("player") PermissionPlayer player, @Argument("permission") String permission) {

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



    @Command("group")
    @Syntax("<group> info")
    public void onGroupInfo(CommandSender sender, @Argument("group") PermissionGroup group) {

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
        sender.sendMessage("§bColor: §7" + group.getColor());
        sender.sendMessage("§bNamePrefix: §7" + group.getNamePrefix());
        sender.sendMessage("§bPrefix: §7" + group.getPrefix());
        sender.sendMessage("§bSuffix: §7" + group.getSuffix());
        sender.sendMessage("§bInherited Groups: §7" + String.join(", ", group.getInheritedGroups()));
        sender.sendMessage("§bPermissions: §7" + group.getPermissions().size());
        sender.sendMessage("§8");
    }

    @Command("group")
    @Syntax("create")
    @CommandExecutionScope(cloud.hytora.driver.command.CommandScope.CONSOLE)
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
                    permissionGroup.setColor(setup.getColor());

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
    @Syntax("<group> delete")
    public void onGroupDelete(CommandSender sender, @Argument("group") PermissionGroup group) {
        if (group == null) {
            sender.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).deletePermissionGroup(group.getName());
        sender.sendMessage("Successfully deleted group {}", group.getName());
    }



    @Command("group")
    @Syntax("<group> perms add <permission>")
    public void onGroupPermissionAdd(CommandSender sender, @Argument("group") PermissionGroup group, @Argument("permission") String permission) {

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
    @Syntax("<group> perms addTemp <permission> <time> <unit>")
    public void onPlayerPermissionAddTemporary(CommandSender sender, @Argument("group") PermissionGroup group, @Argument("permission") String permission, @Argument("time") long time, @Argument("unit") String unit) {

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

    @Command("group")
    @Syntax("<group> perms remove <permission>")
    public void onGroupPermissionRemove(CommandSender sender, @Argument("group") PermissionGroup group, @Argument("permission") String permission) {

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
