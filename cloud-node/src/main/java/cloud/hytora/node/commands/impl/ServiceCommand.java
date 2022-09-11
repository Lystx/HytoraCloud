package cloud.hytora.node.commands.impl;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.help.CommandHelp;
import cloud.hytora.driver.commands.help.CommandHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.deployment.CloudDeployment;
import cloud.hytora.driver.services.deployment.IDeployment;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.utils.ServiceState;

import java.text.SimpleDateFormat;
import java.util.*;

@Command(
        label = "service",
        aliases = {"ser"},
        desc = "Manages all services",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"},
        permission = "cloud.command.service",
        scope = CommandScope.CONSOLE_AND_INGAME
)
public class ServiceCommand {

    public ServiceCommand() {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).registerListener(this);
    }

    @CommandHelp
    public void onArgumentHelp(CommandHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("service");
    }

    @Command(
            label = "list",
            parent = "service",
            desc = "Lists all online services",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void listCommand(CommandContext<?> context, CommandArguments args) {
        context.sendMessage("§8");
        for (ICloudServer service : CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllCachedServices()) {
            context.sendMessage("§b" + service.getName() + " §8[" + service.getServiceState().getName() + " §8/ §7" + service.getServiceVisibility().toString() + "§8] §bSlots §7" + service.getOnlinePlayers().size() + "§8/§7" + service.getMaxPlayers());
        }
        context.sendMessage("§8");
    }


    @Command(
            parent = "service",
            aliases = "dp",
            label = "deploy",
            usage = "<service> <templateName> <excludes>",
            desc = "Copies a service into its template (exclusions are split by ',', put '#' infront for excluded files, '.' for all files and '!' for only these files)",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void deployCommand(CommandContext<?> ctx, CommandArguments args) {

        ICloudServer service = args.get(0, ICloudServer.class);
        String templateName = args.get(1, String.class);
        String excludes = args.get(2, String.class);

        if (service == null) {
            ctx.sendMessage("§cThere is no such Server online!");
            return;
        }

        ITemplate serviceTemplate = service.getTask().getTaskGroup().getTemplates().stream().filter(t -> t.getPrefix().equalsIgnoreCase(templateName)).findFirst().orElse(null);
        if (serviceTemplate == null) {
            ctx.sendMessage("§cThere is no template with name '" + templateName + "' for server " + service.getName() + "!");
            return;
        }

        Collection<String> onlyIncludes = new ArrayList<>();
        Collection<String> excludedFiles = new ArrayList<>();
        if (excludes.startsWith("!")) {
            onlyIncludes = Arrays.asList(excludes.replace("!", "").split(","));
            ctx.sendMessage("§8> §7Only including " + onlyIncludes.toString());
        } else if (excludes.startsWith("$")) {
            onlyIncludes = Arrays.asList(excludes.replace("$", "").split(","));
            ctx.sendMessage("§8> §7Excluding " + excludedFiles.toString());
        } else if (excludes.equalsIgnoreCase(".")) {
            ctx.sendMessage("§8> §7Including every file");
        }

        IDeployment deployment = new CloudDeployment(serviceTemplate, excludedFiles, onlyIncludes);
        service.deploy(deployment);
        ctx.sendMessage("§7Deployed §b" + service.getName() + "§8!");
    }


    @Command(
            parent = "service",
            label = "start",
            usage = "<task> [amount]",
            desc = "Starts new services from a task",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void startCommand(CommandContext<?> ctx, CommandArguments args) {

        IServiceTask task = args.get(0, IServiceTask.class);
        int amount = args.getInt(1, 1);


        if (amount <= 0) {
            ctx.sendMessage("§cPlease provide a number bigger than 0!");
            return;
        }
        for (int i = 0; i < amount; i++) {

            task.configureFutureService()
                    .ignoreIfLimitOfServicesReached()
                    .maxPlayers(task.getDefaultMaxPlayers())
                    .motd(task.getMotd())
                    .node(task.getPossibleNodes().stream().findAny().get())
                    .memory(task.getMemory())
                    .start();
        }

    }

    @Command(
            parent = "service",
            label = "screen",
            usage = "<service>",
            desc = "Joins the screen of a service",
            scope = CommandScope.CONSOLE
    )
    public void screenCommand(CommandContext<?> ctx, CommandArguments args) {

        ICloudServer service = args.get(0, ICloudServer.class);

        if (service == null) {
            ctx.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);

        sm.getScreenByName(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
            @Override
            public void acceptExceptionally(Screen screen) throws Exception {
                screen.registerInputHandler((ExceptionallyConsumer<String>) s -> {
                    if (s.equalsIgnoreCase("leave") || s.equalsIgnoreCase("-l")) {
                       screen.leave();
                    } else {
                        if (s.trim().isEmpty()) {
                            return;
                        }
                        service.sendCommand(s);
                    }
                });

                sm.joinScreen(screen);
            }
        }, () -> ctx.sendMessage("§cNo Screen found for this Service!"));

    }


    @Command(
            parent = "service",
            label = "stop",
            usage = "<service>",
            desc = "Stops a service",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void stopCommand(CommandContext<?> ctx, CommandArguments args) {

        ICloudServer service = args.get(0, ICloudServer.class);

        if (service == null) {
            ctx.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        if (service.getServiceState() == ServiceState.PREPARED || service.getServiceState() == ServiceState.STOPPING) {
            ctx.sendMessage("§cThis service was never started or is already being stopped");
            return;
        }


        ctx.sendMessage("Stopping §b" + service.getName() + "§8...");
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).shutdownService(service);
    }

    @Command(
            parent = "service",
            label = "info",
            usage = "<service>",
            desc = "Shows info about a service",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {
        ICloudServer service = args.get(0, ICloudServer.class);
        if (service == null) {
            ctx.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("Service information:");
        ctx.sendMessage("§bName: §7" + service.getName() + " §8[§3" + service.getTask().getName() + " §8| §3" + service.getTask().getVersion().name() + "§8]");
        ctx.sendMessage("§bAddress: §7" + service.getHostName() + ":" + service.getPort());
        ctx.sendMessage("§bState: " + service.getServiceState().getName());
        ctx.sendMessage("§bVisibility: §7" + service.getServiceVisibility());
        ctx.sendMessage("§bPlayers: §7" + service.getOnlinePlayers().size() + "§8/§7" + service.getMaxPlayers());
        ctx.sendMessage("§bMotd: §7" + service.getPingProperties().getMotd());
        ctx.sendMessage("§bReady: §7" + (service.isReady() ? "§aYes" : "§cNo"));
        ctx.sendMessage("§bUptime: §7" + service.getReadableUptime());
        ctx.sendMessage("§bLast Sync: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp()));
        ctx.sendMessage("§bWould time out at: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp() + CloudDriver.SERVER_PUBLISH_INTERVAL));
        ctx.sendMessage("§bPacket Latency: §7" + service.getLastCycleData().getLatency());
        ctx.sendMessage("§bCycle Data: §7" + service.getLastCycleData().getData().asFormattedJsonString());
        ctx.sendMessage("§8");

    }
}
