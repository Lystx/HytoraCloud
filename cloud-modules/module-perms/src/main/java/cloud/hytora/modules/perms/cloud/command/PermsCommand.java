package cloud.hytora.modules.perms.cloud.command;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.CommandHelp;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.AbstractBundledParameters;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.modules.perms.cloud.setup.GroupSetup;
import cloud.hytora.modules.perms.global.impl.DefaultPermissionPlayer;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Command(
        label = "perms",
        aliases = {"perm"},
        permission = "cloud.command.perms",
        desc = "Manages the perms module",
        scope = CommandScope.CONSOLE_AND_INGAME,
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)
public class PermsCommand {

    @CommandHelp
    public void onArgumentHelp(CommandHelper<?> helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        AbstractBundledParameters args = completer.getParameterSet();
        if (args.isEmpty()) {
            completer.setResult(1, Arrays.asList("user", "group"));
            return;
        }
        String type = args.get(0);
        if (type != null && (type.equalsIgnoreCase("user") || type.equalsIgnoreCase("group"))) {
            if (type.equalsIgnoreCase("group")) {

                completer.setResult(1, completer
                        .getCommand()
                        .getChildrens()
                        .stream()
                        .map(DriverCommand::getLabel)
                        .filter(label -> label.startsWith("group"))
                        .collect(Collectors.toList())
                );
            } else {

                completer.setResult(1, completer
                        .getCommand()
                        .getChildrens()
                        .stream()
                        .map(DriverCommand::getLabel)
                        .filter(label -> label.startsWith("user"))
                        .collect(Collectors.toList())
                );
            }
        }
        //default reactor
        completer.reactWithSubCommands("perms");
    }

    @Command(
            parent = "perms",
            label = "user info",
            usage = "<player>",
            desc = "Shows you information about a player"
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        if (player == null) {
            ctx.sendMessage("§cThere is no such player registered in the modules' database!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("Player information:");
        ctx.sendMessage("§bName: §7" + player.getName() + " §8[§3" + player.getUniqueId() + "§8]");
        ctx.sendMessage("§bHighest Group: §7" + (player.getHighestGroup() == null ? "none" : player.getHighestGroup().getName()));
        ctx.sendMessage("§bAll Groups: §7" + player.getPermissionGroups().stream().map(PermissionGroup::getName).collect(Collectors.joining(", ")));
        ctx.sendMessage("§bPermissions: §7" + player.getPermissions().size());
        ctx.sendMessage("§8");
    }

    @Command(
            parent = "perms",
            label = "user createManually",
            usage = "<player>",
            desc = "Manually creates a player entry"
    )
    public void manuallyCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }

        PermissionPlayer pp = new DefaultPermissionPlayer(player.getName(), player.getUniqueId());
        pp.update();
        ctx.sendMessage("Successfully created User[name={}, uuid={}]", pp.getName(), pp.getUniqueId());
    }

    @Command(
            parent = "perms",
            label = "user addGroup",
            usage = "<player> <group>",
            desc = "Adds a player permanently to a group"
    )
    public void groupAddCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        PermissionGroup group = args.get(1, PermissionGroup.class);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            ctx.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        player.addPermissionGroup(group);
        player.update();
        ctx.sendMessage("Successfully added {} to group {} [PERMANENT]", player.getName(), group.getName());
    }

    @Command(
            parent = "perms",
            label = "user addTempGroup",
            usage = "<player> <group> <time> <unit>",
            desc = "Adds a player temporary to a group"
    )
    public void groupAddTempCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        PermissionGroup group = args.get(1, PermissionGroup.class);
        int time = args.get(2, Integer.class);
        String unit = args.get(3);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            ctx.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        if (time <= 0) {
            ctx.sendMessage("§cPlease provide a time that is bigger than 0!");
        }

        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            player.addPermissionGroup(group, timeUnit, time);
            player.update();
            ctx.sendMessage("Successfully added {} to group {} [{} {}]", player.getName(), group.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            ctx.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command(
            parent = "perms",
            label = "user removeGroup",
            usage = "<player> <group>",
            desc = "Removes a player from a group"
    )
    public void groupRemoveCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        PermissionGroup group = args.get(1, PermissionGroup.class);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (group == null) {
            ctx.sendMessage("§cThere is no such permissionGroup!");
            return;
        }

        if (!player.isInPermissionGroup(group.getName())) {
            ctx.sendMessage("§cThe player does not have the provided PermissionGroup!");
            return;
        }

        player.removePermissionGroup(group.getName());
        player.update();
        ctx.sendMessage("Successfully removed {} from group {}!", player.getName(), group.getName());
    }

    @Command(
            parent = "perms",
            label = "user addPerm",
            usage = "<player> <permission>",
            desc = "Adds a permission to a player"
    )
    public void permsAddCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        String permission = args.get(1);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (player.hasPermission(permission)) {
            ctx.sendMessage("§cThe player already has this permission!");
            return;
        }

        player.addPermission(Permission.of(permission));
        player.update();

        ctx.sendMessage("Successfully added permission {} to player {} [PERMANENT]", permission, player.getName());
    }

    @Command(
            parent = "perms",
            label = "user addTempPerm",
            usage = "<player> <permission> <time> <unit>",
            desc = "Adds a permission temporary to a player"
    )
    public void permsAddTempCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        String permission = args.get(1);
        int time = args.getInt(2);
        String unit = args.get(3);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }

        if (player.hasPermission(permission)) {
            ctx.sendMessage("§cThe player already has this permission!");
            return;
        }

        if (time <= 0) {
            ctx.sendMessage("§cPlease provide a time that is bigger than 0!");
        }

        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            player.addPermission(Permission.of(permission, timeUnit, time));
            player.update();
            ctx.sendMessage("Successfully added permission {} to player {} [{} {}]", permission, player.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            ctx.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command(
            parent = "perms",
            label = "user removePerm",
            usage = "<player> <permission>",
            desc = "Removes a permission from a player"
    )
    public void permsRemoveCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        String permission = args.get(1);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }


        if (!player.hasPermission(permission)) {
            ctx.sendMessage("§cThe player does not have this permission!");
            return;
        }


        player.removePermission(Permission.of(permission));
        player.update();
        ctx.sendMessage("Successfully removed permission {} from player {}!", permission, player.getName());
    }


    @Command(
            parent = "perms",
            label = "user addTaskPerm",
            usage = "<player> <task> <permission>",
            desc = "Adds the provided permission for a given task to a player"
    )
    public void permsTaskAddCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        IServiceTask task = args.get(1, IServiceTask.class);
        String permission = args.get(2);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (task == null) {
            ctx.sendMessage("§cThere is no such task!");
            return;
        }

        if (task.getVersion().isProxy()) {
            ctx.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (player.getTaskPermissions(task.getName()).contains(permission)) {
            ctx.sendMessage("§cThe player already has this taskPermission!");
            return;
        }

        player.addTaskPermission(task, permission);
        player.update();
        ctx.sendMessage("Successfully added permission {} to player {} for task {}!", permission, player.getName(), task.getName());
    }

    @Command(
            parent = "perms",
            label = "user removeTaskPerm",
            usage = "<player> <task> <permission>",
            desc = "Removes the provided permission for a given task from a player"
    )
    public void permsTaskRemoveCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        IServiceTask task = args.get(1, IServiceTask.class);
        String permission = args.get(2);
        if (player == null) {
            ctx.sendMessage("§cThe player has to have at least joined the network yet!");
            return;
        }
        if (task.getVersion().isProxy()) {
            ctx.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }


        if (!player.getTaskPermissions(task.getName()).contains(permission)) {
            ctx.sendMessage("§cThe player does not have this taskPermission!");
            return;
        }

        player.removeTaskPermission(task, permission);
        player.update();
        ctx.sendMessage("Successfully removed permission {} from player {} for task {}!", permission, player.getName(), task.getName());
    }


    @Command(
            parent = "perms",
            label = "user allowPerm",
            usage = "<player> <permission>",
            desc = "Removes the provided permission from the DeniedPermissions-List"
    )
    public void permsAllowPermissionCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        String permission = args.get(1);
        if (player == null) {
            ctx.sendMessage("§cThere is no such player in the module database registered");
            return;
        }
        if (!player.getDeniedPermissions().contains(permission)) {
            ctx.sendMessage("§cThe player is already forbidden to have this permission!");
            return;
        }

        player.removeDeniedPermission(permission);
        player.update();
        ctx.sendMessage("Successfully allowed permission {} for player {} !", permission, player.getName());
    }

    @Command(
            parent = "perms",
            label = "user denyPerm",
            usage = "<player> <permission>",
            desc = "Denies the provided permission for a player"
    )
    public void permsDenyPermissionCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionPlayer player = args.get(0, PermissionPlayer.class);
        String permission = args.get(1);
        if (player == null) {
            ctx.sendMessage("§cThere is no such player in the module database registered");
            return;
        }
        if (player.getDeniedPermissions().contains(permission)) {
            ctx.sendMessage("§cThe player is already forbidden to have this permission!");
            return;
        }

        player.addDeniedPermission(permission);
        player.update();
        ctx.sendMessage("Successfully denied permission {} for player {} !", permission, player.getName());
    }


    @Command(
            parent = "perms",
            label = "group info",
            usage = "<group>",
            desc = "Gives info about a group"
    )
    public void groupInfoCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        if (group == null) {
            ctx.sendMessage("§cThere is no such group registered in the module database!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("Group information:");
        ctx.sendMessage("§bName: §7" + group.getName());
        ctx.sendMessage("§bSortId: §7" + group.getSortId());
        ctx.sendMessage("§bDefaultGroup: §7" + group.isDefaultGroup());
        ctx.sendMessage("§bChatColor: §7" + group.getChatColor());
        ctx.sendMessage("§bColor: §7" + group.getColor());
        ctx.sendMessage("§bNamePrefix: §7" + group.getNamePrefix());
        ctx.sendMessage("§bPrefix: §7" + group.getPrefix());
        ctx.sendMessage("§bSuffix: §7" + group.getSuffix());
        ctx.sendMessage("§bInherited Groups: §7" + String.join(", ", group.getInheritedGroups()));
        ctx.sendMessage("§bPermissions: §7" + group.getPermissions().size());
        ctx.sendMessage("§8");
    }

    @Command(
            parent = "perms",
            label = "group create",
            desc = "Gives info about a group"
    )
    public void groupCreateCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionManager permissionManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);
        new GroupSetup().start((setup, state) -> {
            switch (state) {
                case CANCELLED:
                    ctx.sendMessage("§cNo group was created!");
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

                    ctx.sendMessage("Successfully created PermissionGroup[name={}, id={}]", name, sortId);
                    break;
            }
        });
    }


    @Command(
            parent = "perms",
            label = "group delete",
            usage = "<group>",
            desc = "Gives info about a group"
    )
    public void groupDeleteCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).deletePermissionGroup(group.getName());
        ctx.sendMessage("Successfully deleted group {}", group.getName());
    }


    @Command(
            parent = "perms",
            label = "group addPerm",
            usage = "<group> <permission>",
            desc = "Adds a permission permanent to a group"
    )
    public void groupPermsAddCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        String permission = args.get(1);
        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.hasPermission(permission)) {
            ctx.sendMessage("§cThe group already has this permission!");
            return;
        }

        group.addPermission(Permission.of(permission));
        group.update();

        ctx.sendMessage("Successfully added permission {} to group {} [PERMANENT]", permission, group.getName());
    }

    @Command(
            parent = "perms",
            label = "group addTempPerm",
            usage = "<group> <permission> <time> <unit>",
            desc = "Adds a permission temporary to a group"
    )
    public void groupPermsAddTempCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        String permission = args.get(1);
        int time = args.get(2, Integer.class);
        String unit = args.get(3);
        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.hasPermission(permission)) {
            ctx.sendMessage("§cThe group already has this permission!");
            return;
        }

        if (time <= 0) {
            ctx.sendMessage("§cPlease provide a time that is bigger than 0!");
        }
        try {
            TimeUnit timeUnit = TimeUnit.valueOf(unit);

            group.addPermission(Permission.of(permission, timeUnit, time));
            group.update();
            ctx.sendMessage("Successfully added permission {} to group {} [{} {}]", permission, group.getName(), time, timeUnit.name());
        } catch (IllegalArgumentException e) {
            ctx.sendMessage("§cPlease provide a valid TimeUnit: " + Arrays.asList(TimeUnit.values()).toString().replace("[", "").replace("]", ""));
        }

    }

    @Command(
            parent = "perms",
            label = "group addTaskPerm",
            usage = "<group> <task> <permission>",
            desc = "Adds the provided permission for a given task to a group"
    )
    public void groupPermsAddTaskCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        IServiceTask task = args.get(1, IServiceTask.class);
        String permission = args.get(2);

        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        if (task.getVersion().isProxy()) {
            ctx.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (group.getTaskPermissions(task.getName()).contains(permission)) {
            ctx.sendMessage("§cThe group already has this taskPermission!");
            return;
        }

        group.addTaskPermission(task, permission);
        group.update();
        ctx.sendMessage("Successfully added permission {} to group {} for task {}!", permission, group.getName(), task.getName());
    }

    @Command(
            parent = "perms",
            label = "group removeTaskPerm",
            usage = "<group> <task> <permission>",
            desc = "Removes the provided permission for a given task from a group"
    )
    public void groupPermsRemoveTaskCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        IServiceTask task = args.get(1, IServiceTask.class);
        String permission = args.get(2);

        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }

        if (task.getVersion().isProxy()) {
            ctx.sendMessage("§cTask-Permissions don't work on Proxy-Servers!");
            return;
        }

        if (!group.getTaskPermissions(task.getName()).contains(permission)) {
            ctx.sendMessage("§cThe group does not have this taskPermission!");
            return;
        }

        group.removeTaskPermission(task, permission);
        group.update();
        ctx.sendMessage("Successfully removed permission {} from group {} for task {}!", permission, group.getName(), task.getName());
    }


    @Command(
            parent = "perms",
            label = "group allowPerm",
            usage = "<group> <permission>",
            desc = "Removes the provided permission from the DeniedPermissions-List"
    )
    public void groupPermsAllowCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        String permission = args.get(1);
        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (!group.getDeniedPermissions().contains(permission)) {
            ctx.sendMessage("§cThe group is allowed to have this permission!");
            return;
        }

        group.removeDeniedPermission(permission);
        group.update();
        ctx.sendMessage("Successfully allowed permission {} for group {} !", permission, group.getName());
    }

    @Command(
            parent = "perms",
            label = "group denyPerm",
            usage = "<group> <permission>",
            desc = "Adds the provided permission to the DeniedPermissions-List"
    )
    public void groupPermsDenyCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        String permission = args.get(1);

        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (group.getDeniedPermissions().contains(permission)) {
            ctx.sendMessage("§cThe group is already forbidden to have this permission!");
            return;
        }

        group.addDeniedPermission(permission);
        group.update();
        ctx.sendMessage("Successfully denied permission {} for group {} !", permission, group.getName());
    }

    @Command(
            parent = "perms",
            label = "group removePerm",
            usage = "<group> <permission>",
            desc = "Removes a permission from the group"
    )
    public void groupPermsRemoveCommand(CommandContext<?> ctx, CommandArguments args) {
        PermissionGroup group = args.get(0, PermissionGroup.class);
        String permission = args.get(1);

        if (group == null) {
            ctx.sendMessage("§cThere is no such group in the module database registered");
            return;
        }
        if (!group.hasPermission(permission)) {
            ctx.sendMessage("§cThe group does not have this permission!");
            return;
        }

        group.removePermission(Permission.of(permission));
        group.update();
        ctx.sendMessage("Successfully removed permission {} from group {}!", permission, group.getName());
    }

}
