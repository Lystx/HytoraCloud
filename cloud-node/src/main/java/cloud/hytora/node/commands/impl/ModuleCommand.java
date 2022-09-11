package cloud.hytora.node.commands.impl;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.CommandHelp;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.module.IModuleManager;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.node.module.ModuleDownloader;
import cloud.hytora.node.module.NodeModuleManager;
import cloud.hytora.node.module.updater.ModuleInfo;

import java.util.*;

@Command(
        label = "module",
        aliases = {"modules"},
        desc = "Manages all modules",
        scope = CommandScope.CONSOLE_AND_INGAME,
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"},
        permission = "cloud.command.modules"
)
public class ModuleCommand {

    private final ModuleDownloader downloader = new ModuleDownloader();

    @CommandHelp
    public void onArgumentHelp(CommandHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("module");
    }

    @Command(
            parent = "module",
            label = "info",
            usage = "<name>",
            desc = "Gives info on a module"
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {

        String name = args.getString(0);

        IModuleManager moduleManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);

        ModuleController module = moduleManager.getModuleByNameOrNull(name);
        if (module == null) {
            ctx.sendMessage("§cThere is no module loaded with the name §e" + name + "§c!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("Module information:");
        ctx.sendMessage("§bStatus: §7" + module.getState());
        ctx.sendMessage("§bName: §7" + module.getModuleConfig().getName());
        ctx.sendMessage("§bAuthor: §7" + Arrays.toString(module.getModuleConfig().getAuthor()));
        ctx.sendMessage("§bMain-Class: §7" + module.getModuleConfig().getMainClass());
        ctx.sendMessage("§bVersion: §7" + module.getModuleConfig().getVersion());
        ctx.sendMessage("§bWebsite: §7" + module.getModuleConfig().getWebsite());
        ctx.sendMessage("§bCopy-Type: §7" + module.getModuleConfig().getCopyType());
        ctx.sendMessage("§bEnvironment: §7" + module.getModuleConfig().getEnvironment());
        ctx.sendMessage("§bDepends: §7" + Arrays.toString(module.getModuleConfig().getDepends()));
        ctx.sendMessage("§8");
    }
    @Command(
            parent = "module",
            label = "list",
            desc = "Lists all modules (append -online to see all online available)",
            flags = {"online"}
    )
    public void listCommand(CommandContext<?> ctx, CommandArguments args) {

        if (args.hasFlag("online")) {

            Collection<ModuleInfo> modules = downloader.loadProvidedModules();
            if (modules.isEmpty()) {
                ctx.sendMessage("§cThere are currently no modules available to download!");
                return;
            }

            ctx.sendMessage("§8");
            ctx.sendMessage("§7Modules (" + modules.size() + ")§8:");

            for (ModuleInfo module : modules) {
                ctx.sendMessage("§b" + module.getName() + " §8[§e" + module.getVersion() + "§8]");
            }
            ctx.sendMessage("§8");
            return;
        }
        List<ModuleController> modules = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class).getModules();

        if (modules.isEmpty()) {
            ctx.sendMessage("§cThere are currently no modules loaded!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("§7Modules (" + modules.size() + ")§8:");

        for (ModuleController module : modules) {
            ModuleConfig config = module.getModuleConfig();
            ctx.sendMessage("§b" + config.getName() + " §8[" + (module.getState() == ModuleState.ENABLED ? "§aEnabled" : "§cDisabled") + "§8] §8[§e" + config.getVersion() + " | " + Arrays.toString(config.getAuthor()) + " | " + config.getDescription() + "§8]");
        }
        ctx.sendMessage("§8");
    }

    @Command(
            parent = "module",
            label = "unload",
            usage = "<name>",
            desc = "Unloads a module",
            flags = {"confirm"}
    )
    public void unloadModules(CommandContext<?> ctx, CommandArguments args) {
        if (!args.hasFlag("confirm")) {
            ctx.sendMessage("§cPlease append §e-confirm §cto the command to confirm that you really want to unload!");
            ctx.sendMessage("§cMake sure that the module you are unloading is not necessary for the network or that no players are on the network!");
            return;
        }

        String name = args.getString(0);
        IModuleManager moduleManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);

        ModuleController module = moduleManager.getModuleByNameOrNull(name);
        if (module == null) {
            ctx.sendMessage("§cThere is no module loaded with the name §e" + name + "§c!");
            return;
        }
        if (module.getState() == ModuleState.UNREGISTERED | module.getState() == ModuleState.DISABLED) {
            ctx.sendMessage("§cThe module is already unloaded!");
            return;
        }
        module.disableModule();
        module.unregisterModule();
        ctx.sendMessage("Unregistered module!");
    }
    @Command(
            parent = "module",
            label = "load",
            usage = "<name>",
            desc = "Loads a module"
    )
    public void loadModules(CommandContext<?> ctx, CommandArguments args) {
        String name = args.getString(0);
        IModuleManager moduleManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);

        ModuleController module = moduleManager.getModuleByNameOrNull(name);
        if (module == null) {
            ctx.sendMessage("§cThere is no existing module with the name §e" + name + "§c!");
            return;
        }
        if (module.getState() != ModuleState.UNREGISTERED && module.getState() != ModuleState.DISABLED) {
            ctx.sendMessage("§cThe module is already enabled!");
            return;
        }

        if (!module.getModuleConfig().getEnvironment().applies(CloudDriver.getInstance().getEnvironment())) {
            ctx.sendMessage("Skipping initialization of {} (ModuleEnvironment.{}, DriverEnvironment.{})",
                    module, module.getModuleConfig().getEnvironment(), CloudDriver.getInstance().getEnvironment());
            return;
        }

        try {
            module.initConfig();
            module.initModule();
            module.loadModule();
            module.enableModule();
            ctx.sendMessage("Loaded module!");
        } catch (Exception e) {
            ctx.sendMessage("§cCouldn't load module!");
        }
    }


    @Command(
            parent = "module",
            label = "download",
            usage = "[name]",
            desc = "Downloads all or one module"
    )
    public void downloadCommand(CommandContext<?> ctx, CommandArguments args) {
        String name = args.getString(0, "ALL");

        if (name.equalsIgnoreCase("ALL")) {
            Logger.constantInstance().info("Downloading all modules...");
        } else {
            NodeModuleManager moduleManager = (NodeModuleManager) CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);

            Collection<ModuleInfo> modules = downloader.loadProvidedModules();
            ModuleInfo moduleInfo = modules.stream().filter(module -> module.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
            if (moduleInfo == null) {
                ctx.sendMessage("§cThere is no such module to download!");
                ctx.sendMessage("§cUse §emodule list -online §cto see all available modules!");
                return;
            }
            downloader
                    .downloadModule(moduleInfo, downloader.getModuleUrl(moduleInfo))
                    .onTaskSucess(path -> {
                        ctx.sendMessage("§7Downloaded module!");
                        ctx.sendMessage("§7Auto-resolving, loading and enabling module...");
                        ModuleController module = moduleManager.resolveSingleModule(path, new ArrayList<>());
                        try {
                            module.initConfig();
                            module.initModule();
                            module.loadModule();
                            module.enableModule();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).onTaskFailed(e -> {
                       ctx.sendMessage("§cCouldn't download module!");
                    });
        }
    }

    @Command(
            parent = "module",
            label = "update",
            usage = "[name]",
            desc = "Updates all modules or a single one",
            flags = {"confirm"}
    )
    public void checkForUpdates(CommandContext<?> ctx, CommandArguments args) {
        if (!args.hasFlag("confirm")) {
            ctx.sendMessage("§cPlease append §e-confirm §cto the command to confirm that you really want to update!");
            ctx.sendMessage("§cMake sure that the module(s) you are updating are not necessary for the network or that no players are on the network!");
            return;
        }
        String name = args.getString(0, "ALL");
        IModuleManager moduleManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IModuleManager.class);

        if (name.equalsIgnoreCase("ALL")) {
            ctx.sendMessage("Trying to update ALL modules!");
            ctx.sendMessage("Unregistering all modules to avoid runtime-conflicts");
            moduleManager.disableModules();
            moduleManager.unregisterModules();
            downloader
                    .updateModules()
                    .onTaskSucess(n -> {
                        ctx.sendMessage("Updated " + n + " modules(s)!");
                    }).onTaskFailed(e -> {
                        ctx.sendMessage("Couldn't update modules!");
                        e.printStackTrace();
                    });
        } else {
            ModuleController module = moduleManager.getModuleByNameOrNull(name);
            if (module == null) {
                ctx.sendMessage("§cThere is no module loaded with the name §e" + name + "§c!");
                return;
            }
            ctx.sendMessage("Trying to update Module '" + name + "'");
            ctx.sendMessage("Unregistering module to avoid runtime-conflicts");
            module.disableModule();
            module.unregisterModule();
            downloader
                    .updateModule(name)
                    .onTaskSucess(n -> {
                        ctx.sendMessage("Updated module to version {}!", n.getVersion().toString());
                    }).onTaskFailed(e -> {
                        ctx.sendMessage("Couldn't update module!");
                        e.printStackTrace();
                    });
        }
    }
}
