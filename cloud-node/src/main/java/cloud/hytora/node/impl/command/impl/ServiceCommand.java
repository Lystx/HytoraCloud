package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.misc.Util;
import cloud.hytora.context.annotations.ApplicationParticipant;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.HytoraCloudConstants;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.impl.CloudServerCompleter;
import cloud.hytora.driver.command.completer.impl.TaskCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRequestScreenLeaveEvent;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.deployment.CloudDeployment;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.ServiceState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Command(
        value = {"service", "ser"},
        permission = "cloud.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all services"
)
@Command.AutoHelp
@ApplicationParticipant
public class ServiceCommand {

    public ServiceCommand() {
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    @EventListener
    public void handleQuit(ServiceRequestScreenLeaveEvent event) {
        CloudDriver.getInstance().getProvider(ScreenManager.class).leaveCurrentScreen();
    }

    @Command(value = "list", description = "Lists all online services")
    public void onListCommand(CommandSender sender) {
        sender.sendMessage("§8");
        for (ICloudService service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            sender.sendMessage("§b" + service.getName() + " §8[" + service.getServiceState().getName() + " §8/ §7" + service.getServiceVisibility().toString() + "§8] §bSlots §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
        }
        sender.sendMessage("§8");
    }

    @Command(value = "deploy", description = "Copies a service into its template (exclusions are split by ',', put '#' infront for excluded files, '.' for all files and '!' for only these files)")
    @Command.Syntax("<service> <templateName> <excludes>")
    public void onDeployCommand(
            CommandSender sender,
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) ICloudService service,
            @Command.Argument("templateName") String templateName,
            @Command.Argument("excludes") String excludes
    ) {

        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ServiceTemplate serviceTemplate = service.getTask().getTaskGroup().getTemplates().stream().filter(t -> t.getPrefix().equalsIgnoreCase(templateName)).findFirst().orElse(null);
        if (serviceTemplate == null) {
            sender.sendMessage("§cThere is no template with name '" + templateName + "' for server " + service.getName() + "!");
            return;
        }

        Collection<String> onlyIncludes = new ArrayList<>();
        Collection<String> excludedFiles = new ArrayList<>();
        if (excludes.startsWith("!")) {
            onlyIncludes = Arrays.asList(excludes.replace("!", "").split(","));
            sender.sendMessage("§8> §7Only including " + onlyIncludes.toString());
        } else if (excludes.startsWith("$")) {
            onlyIncludes = Arrays.asList(excludes.replace("$", "").split(","));
            sender.sendMessage("§8> §7Excluding " + excludedFiles.toString());
        } else if (excludes.equalsIgnoreCase(".")) {
            sender.sendMessage("§8> §7Including every file");
        }

        ServiceDeployment deployment = new CloudDeployment(serviceTemplate, excludedFiles, onlyIncludes);
        service.deploy(deployment);
        sender.sendMessage("§7Deployed §b" + service.getName() + "§8!");
    }


    @Command(value = "start", description = "Starts an amount of services of given task")
    @Command.Syntax("<task> <amount>")
    public void onStartCommand(
            CommandSender sender,
            @Command.Argument(value = "task", completer = TaskCompleter.class) IServiceTask task,
            @Command.Argument("amount") int amount

    ) {

        if (amount <= 0) {
            sender.sendMessage("§cPlease provide a number bigger than 0!");
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

    @Command(value = "upload", description = "Uploads the logs of the given service and sends the link")
    @Command.Syntax("<service>")
    public void onUploadCommand(
            CommandSender sender,
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) ICloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);

        sm.getScreenByName(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
            @Override
            public void acceptExceptionally(Screen screen) throws Exception {
                String uploadedLink = Util.uploadToHastebin(screen.getAllCachedLines());

                sender.sendMessage("§7Log was §auploaded §7to §e" + uploadedLink);
            }
        }, () -> sender.sendMessage("§cNo Screen found for this Service!"));

    }

    @Command(value = "screen", description = "Joins the Output of a server")
    @Command.Syntax("<service>")
    public void onScreenCommand(
            CommandSender sender,
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) ICloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);

        sm.getScreenByName(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
            @Override
            public void acceptExceptionally(Screen screen) throws Exception {
                screen.addInputHandler((ExceptionallyConsumer<String>) s -> {
                    if (s.equalsIgnoreCase("leave") || s.equalsIgnoreCase("-l")) {        CloudDriver.getInstance().getProvider(ScreenManager.class).leaveCurrentScreen();

                    } else {
                        if (s.trim().isEmpty()) {
                            return;
                        }
                        // sender.sendMessage("Executing '{}' on {}", s, service.getName());
                        service.sendCommand(s);
                    }
                });

                sm.joinScreen(screen);
            }
        }, () -> sender.sendMessage("§cNo Screen found for this Service!"));

    }


    @Command(value = "stop", description = "Stops a service")
    @Command.Syntax("<name>")
    public void onStopCommand(
            CommandSender sender,
            @Command.Argument(value = "name", completer = CloudServerCompleter.class) ICloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        if (service.getServiceState() == ServiceState.PREPARED || service.getServiceState() == ServiceState.STOPPING) {
            sender.sendMessage("§cThis service was never started or is already being stopped");
            return;
        }


        sender.sendMessage("Stopping §b" + service.getName() + "§8...");
        CloudDriver.getInstance().getServiceManager().shutdownService(service);
    }

    @Command(value = "info", description = "Shows info about a service")
    @Command.Syntax("<name>")
    public void onInfoCommand(
            CommandSender sender,
            @Command.Argument(value = "name", completer = CloudServerCompleter.class) ICloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("Service information:");
        sender.sendMessage("§bName: §7" + service.getName() + " §8[§3" + service.getTask().getName() + " §8| §3" + service.getTask().getVersion().name() + "§8]");
        sender.sendMessage("§bAddress: §7" + service.getHostName() + ":" + service.getPort());
        sender.sendMessage("§bState: " + service.getServiceState().getName());
        sender.sendMessage("§bVisibility: §7" + service.getServiceVisibility());
        sender.sendMessage("§bPlayers: §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
        sender.sendMessage("§bMotd: §7" + service.getMotd());
        sender.sendMessage("§bReady: §7" + (service.isReady() ? "§aYes" : "§cNo"));
        sender.sendMessage("§bUptime: §7" + service.getReadableUptime());
        sender.sendMessage("§bLast Sync: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp()));
        sender.sendMessage("§bWould time out at: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp() + HytoraCloudConstants.SERVER_PUBLISH_INTERVAL));
        sender.sendMessage("§bPacket Latency: §7" + service.getLastCycleData().getLatency());
        sender.sendMessage("§bCycle Data: §7" + service.getLastCycleData().getData().asFormattedJsonString());
        sender.sendMessage("§8");

    }
}
