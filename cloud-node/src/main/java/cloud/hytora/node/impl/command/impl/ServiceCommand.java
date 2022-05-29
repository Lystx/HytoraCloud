package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandManager;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.CloudServerCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.event.CloudEventHandler;
import cloud.hytora.driver.event.defaults.server.CloudServerRequestScreenLeaveEvent;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.deployment.CloudDeployment;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.ServiceState;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Command(
        name = {"service", "ser"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandDescription("Manages all services")
@CommandAutoHelp
public class ServiceCommand {

    public ServiceCommand() {
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    @CloudEventHandler
    public void handleQuit(CloudServerRequestScreenLeaveEvent event) {
        this.leaveScreen(event.getCommandManager(), event.getConsole(), event.getSender(), event.getService());
    }

    @SubCommand("list")
    @CommandDescription("Lists all online services")
    public void onListCommand(CommandSender sender) {
        sender.sendMessage("§8");
        for (CloudServer service : CloudDriver.getInstance().getServiceManager().getAllCachedServices()) {
            sender.sendMessage("§b" + service.getName() + " §8[" + service.getServiceState().getName() + " §8/ §7" + service.getServiceVisibility().toString() + "§8] §bSlots §7" + service.getOnlinePlayers() + "§8/§7" + service.getMaxPlayers());
        }
        sender.sendMessage("§8");
    }

    @SubCommand("deploy <service> <templateName> <excludes>")
    @CommandDescription("Copies a service into its template (exclusions are split by ';', use '#' to include all)")
    public void onDeployCommand(
            CommandSender sender,
            @CommandArgument(value = "service", completer = CloudServerCompleter.class) CloudServer service,
            @CommandArgument("templateName") String templateName,
            @CommandArgument("excludes") String excludes
    ) {

        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ServiceTemplate serviceTemplate = service.getConfiguration().getParent().getTemplates().stream().filter(t -> t.getPrefix().equalsIgnoreCase(templateName)).findFirst().orElse(null);
        if (serviceTemplate == null) {
            sender.sendMessage("§cThere is no template with name '" + templateName + "' for server " + service.getName() + "!");
            return;
        }

        ServiceDeployment deployment = new CloudDeployment(serviceTemplate, (excludes.equals("#") ? new ArrayList<>() : Arrays.asList(excludes.split(";"))));
        service.deploy(deployment);
        sender.sendMessage("§7Deployed §b" + service.getName() + "§8!");
    }


    @SubCommand("screen <service> <join/leave>")

    public void onScreenCommand(
            CommandSender sender,
            @CommandArgument(value = "service", completer = CloudServerCompleter.class) CloudServer service,
            @CommandArgument("join/leave") String type
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        Console console = Objects.requireNonNull(CloudDriver.getInstance().getConsole());
        CommandManager commandManager = CloudDriver.getInstance().getCommandManager();

        if (type.equalsIgnoreCase("join")) {

            console.clearScreen();
            List<String> cachedLines = service.queryServiceOutput();
            sender.sendMessage("§8");
            sender.sendMessage("§7Joining screen of §a" + service.getName() + "§8...");
            sender.sendMessage("§7To exit the screen type §8'§cleave§8'");
            sender.sendMessage("§8");

            service.asCloudServer().setScreenServer(true);
            console.setLineCaching(false);
            commandManager.setActive(false);
            commandManager.setInActiveHandler((commandSender, s) -> {
                if (s.equalsIgnoreCase("leave") || s.equalsIgnoreCase("-l")) {
                    this.leaveScreen(commandManager, console, sender, service);
                } else {
                    sender.sendMessage("§7Sending Command-Line §8'§e" + s + "§8' §7to service§8...");
                    service.executeCommand(s);
                }
            });

            for (String cachedLine : cachedLines) {
                sender.sendMessage(cachedLine);
            }

        } else if (type.equalsIgnoreCase("leave")) {
            this.leaveScreen(commandManager, console, sender, service);
        } else {
            sender.sendMessage("§cscreen " + service.getName() + " <join/leave>");
        }
    }

    private void leaveScreen(CommandManager commandManager, Console console, CommandSender sender, CloudServer service) {

        console.clearScreen();
        for (String allWroteLine : console.getAllWroteLines()) {
            console.forceWrite(allWroteLine);
        }
        console.setLineCaching(true);
        commandManager.setInActiveHandler(null);
        commandManager.setActive(true);

        sender.sendMessage("§8");
        sender.sendMessage("§7You left screen §c" + service.getName() + "§8!");
        sender.sendMessage("§8");

        service.asCloudServer().setScreenServer(false);
    }

    @SubCommand("stop <name>")
    @CommandDescription("Stops a service")
    public void onStopCommand(
            CommandSender sender,
            @CommandArgument(value = "name", completer = CloudServerCompleter.class) CloudServer service
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

    @SubCommand("info <name>")
    @CommandDescription("Shows info about a service")
    public void onInfoCommand(
            CommandSender sender,
            @CommandArgument(value = "name", completer = CloudServerCompleter.class) CloudServer service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("Service information:");
        sender.sendMessage("§bName: §7" + service.getName() + " §8[§3" + service.getConfiguration().getName() + " §8| §3" + service.getConfiguration().getVersion().name() + "§8]");
        sender.sendMessage("§bAddress: §7" + service.getHostName() + ":" + service.getPort());
        sender.sendMessage("§bState: " + service.getServiceState().getName());
        sender.sendMessage("§bVisibility: §7" + service.getServiceVisibility());
        sender.sendMessage("§bPlayers: §7" + service.getOnlinePlayers() + "§8/§7" + service.getMaxPlayers());
        sender.sendMessage("§bMotd: §7" + service.getMotd());
        sender.sendMessage("§bReady: §7" + (service.isReady() ? "§aYes" : "§cNo"));
        sender.sendMessage("§8");

    }
}
