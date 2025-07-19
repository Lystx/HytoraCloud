package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.scheduler.Scheduler;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.impl.ModuleCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.ModuleManager;

import java.io.File;
import java.util.Arrays;

@Command(
        value = {"mod", "pl", "plugin", "module"},
        permission = "cloud.hytora.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all modules"
)
@Command.AutoHelp

public class ModuleCommand {


    @Command(value = "list", description = "Lists all modules")
    public void onListCommand(CommandSender sender) {
        sender.sendMessage("§8");
        for (ModuleController module : CloudDriver.getInstance().getModuleManager().getModules()) {
            sender.sendMessage("%1" + module.getModuleConfig().getName() + " §8[§a" + module.getState().name() + "§8]");
        }
        sender.sendMessage("§8");
    }

    @Command(value = "unload", description = "Unloads the provided module")
    @Command.Syntax("<module>")
    public void unloadCommand(
            CommandSender sender,
            @Command.Argument(value = "module", completer = ModuleCompleter.class) ModuleController module
    ) {
        if (module == null) {
            sender.sendMessage("§cThere is no such module loaded!");
            return;
        }

        ModuleManager moduleManager = CloudDriver.getInstance().getModuleManager();

        sender.sendMessage("§7Disabling the module §e" + module.getModuleConfig().getName() + "§f...");
        module.disableModule();
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            module.unregisterModule();
            moduleManager.removeModule(module);

            sender.sendMessage("§cThe Module was unloaded!");
        }, 50L);

    }

    @Command(value = "load", description = "Loads a module from file")
    @Command.Syntax("<file>")
    public void loadCommand(
            CommandSender sender,
            @Command.Argument(value = "file") String file
    ) {
        File parent = CloudDriver.Constants.MODULE_FOLDER;
        File moduleFile = new File(parent, file);
        if (!moduleFile.exists()) {
            sender.sendMessage("§cThis File does not exist in the §eModule-Folder§c!");
            return;
        }

        ModuleManager moduleManager = CloudDriver.getInstance().getModuleManager();

        sender.sendMessage("§7Resolving the module §6" + moduleFile + "§f...");
        ModuleController module = moduleManager.resolveModule(moduleFile.toPath());

        sender.sendMessage("§7Loading the module §e" + module.getModuleConfig().getFullName() + "§f...");
        module.loadModule();
        sender.sendMessage("§7Enabling the module §a" + module.getModuleConfig().getFullName() + "§f...");
        module.enableModule();
        sender.sendMessage("§aDone§8!");

    }

    @Command(value = "info", description = "Unloads the provided module")
    @Command.Syntax("<module>")
    public void infoCommand(
            CommandSender sender,
            @Command.Argument(value = "module", completer = ModuleCompleter.class) ModuleController module
    ) {
        if (module == null) {
            sender.sendMessage("§cThere is no such module loaded!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("Module information:");
        sender.sendMessage("  §8» %1Name: §7" + module.getModuleConfig().getName());
        sender.sendMessage("  §8» %1Author: §7" + Arrays.toString(module.getModuleConfig().getAuthor()));
        sender.sendMessage("  §8» %1Version: §7" + module.getModuleConfig().getVersion());
        sender.sendMessage("  §8» %1Description: §7" + module.getModuleConfig().getDescription());
        sender.sendMessage("  §8» %1CopyType: §7" + module.getModuleConfig().getCopyType());
        sender.sendMessage("  §8» %1Environment: §7" + module.getModuleConfig().getEnvironment());
        sender.sendMessage("  §8» %1Website: §7" + module.getModuleConfig().getWebsite());
        sender.sendMessage("  §8» %1Jar-File: §7" + module.getJarFile());
        sender.sendMessage("  §8» %1Json Config:");
        sender.sendMessage("§a" + module.getConfig().asFormattedJsonString());
        sender.sendMessage("§8");

    }

    @Command(value = "reload", description = "Reloads a module")
    @Command.Syntax("<name>")
    public void onInfoCommand(
            CommandSender sender,
            @Command.Argument(value = "name", completer = ModuleCompleter.class) ModuleController module
    ) {
        if (module == null) {
            sender.sendMessage("§cThere is no loaded module matching this name!");
            return;
        }

        ModuleManager moduleManager = CloudDriver.getInstance().getModuleManager();

        sender.sendMessage("§7Disabling the module §e" + module.getModuleConfig().getName() + "§f...");
        module.disableModule();
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            module.unregisterModule();
            moduleManager.removeModule(module);

            sender.sendMessage("§cThe Module was unloaded!");
            Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {

                moduleManager.resolveModules();
                moduleManager.loadModules();

                //enabling modules after having loaded the database
                moduleManager.enableModules();
            }, 50L);
        }, 50L);

    }
}
