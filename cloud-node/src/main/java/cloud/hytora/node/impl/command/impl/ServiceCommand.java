package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.CloudServerCompleter;
import cloud.hytora.driver.command.completer.TaskCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.console.Screen;
import cloud.hytora.driver.console.ScreenManager;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceRequestScreenLeaveEvent;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.deployment.CloudDeployment;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.ServiceState;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

@CommandDescription("Manages all services")
@Command({"service", "ser"})
@CommandExecutionScope(CommandScope.CONSOLE_AND_INGAME)
@CommandPermission("cloud.command.use")
@CommandAutoHelp
public class ServiceCommand {

    public ServiceCommand() {
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    @EventListener
    public void handleQuit(ServiceRequestScreenLeaveEvent event) {
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class).leaveCurrentScreen();
    }

    @Command("list")
    @CommandDescription("Lists all online services")
    public void onListCommand(CommandSender sender) {
        sender.sendMessage("§8");
        for (ICloudServer service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            sender.sendMessage("§b" + service.getName() + " §8[" + service.getServiceState().getName() + " §8/ §7" + service.getServiceVisibility().toString() + "§8] §bSlots §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
        }
        sender.sendMessage("§8");
    }

    @Command("deploy")
    @Syntax("<service> <templateName> <excludes>")
    @CommandDescription("Copies a service into its template (exclusions are split by ';', use '#' to include all)")
    public void onDeployCommand(
            CommandSender sender,
            @Argument(value = "service", completer = CloudServerCompleter.class) ICloudServer service,
            @Argument("templateName") String templateName,
            @Argument("excludes") String excludes
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

        ServiceDeployment deployment = new CloudDeployment(serviceTemplate, (excludes.equals("#") ? new ArrayList<>() : Arrays.asList(excludes.split(";"))));
        service.deploy(deployment);
        sender.sendMessage("§7Deployed §b" + service.getName() + "§8!");
    }


    @Command("start")
    @Syntax("<task> <amount>")
    public void onStartCommand(
            CommandSender sender,
            @Argument(value = "task", completer = TaskCompleter.class) IServiceTask task,
            @Argument("amount") int amount

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

    @Command("screen")
    @Syntax("<service>")
    public void onScreenCommand(
            CommandSender sender,
            @Argument(value = "service", completer = CloudServerCompleter.class) ICloudServer service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);

        sm.getScreenByName(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
            @Override
            public void acceptExceptionally(Screen screen) throws Exception {
                screen.addInputHandler((ExceptionallyConsumer<String>) s -> {
                    if (s.equalsIgnoreCase("leave") || s.equalsIgnoreCase("-l")) {        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class).leaveCurrentScreen();

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


    @Command("stop")
    @Syntax("<name>")
    @CommandDescription("Stops a service")
    public void onStopCommand(
            CommandSender sender,
            @Argument(value = "name", completer = CloudServerCompleter.class) ICloudServer service
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

    @Command("info")
    @Syntax("<name>")
    @CommandDescription("Shows info about a service")
    public void onInfoCommand(
            CommandSender sender,
            @Argument(value = "name", completer = CloudServerCompleter.class) ICloudServer service
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
        sender.sendMessage("§bWould time out at: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp() + CloudDriver.SERVER_PUBLISH_INTERVAL));
        sender.sendMessage("§bPacket Latency: §7" + service.getLastCycleData().getLatency());
        sender.sendMessage("§bCycle Data: §7" + service.getLastCycleData().getData().asFormattedJsonString());
        sender.sendMessage("§8");

    }
}
