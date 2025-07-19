package cloud.hytora.node.impl.command.impl;

import cloud.hytora.common.function.ExceptionallyConsumer;
import cloud.hytora.common.misc.Util;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.impl.CloudServerCompleter;
import cloud.hytora.driver.command.completer.impl.TaskCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.command.console.screen.Screen;
import cloud.hytora.driver.command.console.screen.ScreenManager;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.entity.node.INode;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.NodeSpecificCloudService;
import cloud.hytora.driver.entity.services.deployment.CloudDeployment;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.utils.ServiceState;
import cloud.hytora.driver.entity.services.utils.ServiceVisibility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@Command(
        value = {"service", "ser"},
        permission = "cloud.hytora.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all services"
)
@Command.AutoHelp

public class ServiceCommand {

    public ServiceCommand() {
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    @Command(value = "list", description = "Lists all online services (ALL for all nodes)")
    @Command.Syntax("<node>")
    public void onListCommandSecond(CommandSender sender, @Command.Argument("node") String node) {
        String msg = node.equalsIgnoreCase("ALL") ? "All Services" : "Services of %2" + node;

        sender.sendMessage("§8");
        sender.sendMessage("§8<=====§8[%1{}§8]=====>", msg);
        sender.sendMessage("§8");

        if (node.equalsIgnoreCase("ALL")) {
            for (INode cachedNode : CloudDriver.getInstance().getNodeManager().getAllCachedNodes()) {
                sender.sendMessage("  §8=> %1Services of Node %2{} §8[%1{}§8]§8:", cachedNode.getName(), cachedNode.getAssignedServers().size());
                for (CloudService service : cachedNode.getAssignedServers()) {
                    sender.sendMessage("    §8=> %1" + service.getName() + " §8[" + service.getServiceState().getName() + " §8| §7" + service.getServiceVisibility().toString() + " §8| §7Ready§8: " + (service.isReady() ? "§aYes" : "§cNo") + "§8] %1Slots §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
                }
            }
        } else {
            INode n = CloudDriver.getInstance().getNodeManager().getCachedNode(node);
            if (n == null) {
                sender.sendMessage("§cThere is no Node with the name §e{} §cconnected, so unfortunately no Services to be listed.", node);
                return;
            } else {
                for (CloudService service : n.getAssignedServers()) {
                    sender.sendMessage("%1" + service.getName() + " §8[" + service.getServiceState().getName() + " §8| §7" + service.getServiceVisibility().toString() + " §8| §7Ready§8: " + (service.isReady() ? "§aYes" : "§cNo") + "§8] %1Slots §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
                }
            }
        }

        sender.sendMessage("§8");
        sender.sendMessage("§8<=====§8[%1{}§8]=====>", msg);
        sender.sendMessage("§8");
    }

    @Command(value = "deploy", description = "Copies a service into its template (exclusions are split by ',', put '#' infront for excluded files, '.' for all files and '!' for only these files)")
    @Command.Syntax("<service> <templateName> <excludes>")
    public void onDeployCommand(
            CommandSender sender,
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) CloudService service,
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
        sender.sendMessage("§7Deployed %1" + service.getName() + "§8!");
    }


    @Command(value = "start", description = "Starts an amount of services of given task")
    @Command.Syntax("<task> <amount>")
    public void onStartCommand(
            CommandSender sender,
            @Command.Argument(value = "task", completer = TaskCompleter.class) ServiceTask task,
            @Command.Argument("amount") int amount

    ) {

        if (task == null) {
            sender.sendMessage("§cPlease provide a valid §eServiceTask§c!");
            return;
        }
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
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) CloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);

        sm.getScreen(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
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
            @Command.Argument(value = "service", completer = CloudServerCompleter.class) CloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no such Server online!");
            return;
        }

        ScreenManager sm = CloudDriver.getInstance().getProvider(ScreenManager.class);

        sm.getScreen(service.getName()).ifPresentOrElse(new ExceptionallyConsumer<Screen>() {
            @Override
            public void acceptExceptionally(Screen screen) throws Exception {
                screen.addInputHandler((ExceptionallyConsumer<String>) s -> {
                    if (s.equalsIgnoreCase("leave") || s.equalsIgnoreCase("-l")) {
                        CloudDriver.getInstance().getProvider(ScreenManager.class).leaveCurrentScreen();
                    } else {
                        if (s.trim().isEmpty()) {
                            return;
                        }
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
            @Command.Argument(value = "name", completer = CloudServerCompleter.class) CloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }

        if (service.getServiceState() == ServiceState.PREPARED || service.getServiceState() == ServiceState.STOPPING) {
            sender.sendMessage("§cThis service was never started or is already being stopped");
            return;
        }


        sender.sendMessage("Stopping %1" + service.getName() + "§8...");
        CloudDriver.getInstance().getServiceManager().shutdownService(service);
    }

    @Command(value = "toggleVisibility", description = "Toggles the visibility of a service")
    @Command.Syntax("<name>")
    public void onToggleVisibility(
            CommandSender sender,
            @Command.Argument(value = "name", completer = CloudServerCompleter.class) CloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }


        NodeSpecificCloudService pcs = (NodeSpecificCloudService) service;
        ServiceVisibility opposite = pcs.getServiceVisibility().opposite();
        pcs.setServiceVisibility(opposite);
        pcs.update(PublishingType.GLOBAL);
        sender.sendMessage("§7You changed the visibility to %2{}", opposite);
    }

    @Command(value = "info", description = "Shows info about a service")
    @Command.Syntax("<name>")
    public void onInfoCommand(
            CommandSender sender,
            @Command.Argument(value = "name", completer = CloudServerCompleter.class) CloudService service
    ) {
        if (service == null) {
            sender.sendMessage("§cThere is no online service matching this name!");
            return;
        }


        NodeSpecificCloudService pcs = (NodeSpecificCloudService) service;

        sender.sendMessage("§8");
        sender.sendMessage("Service information:");
        sender.sendMessage("  §8» %1Name: §7" + service.getName() + " §8[%2" + service.getTask().getName() + " §8| %2" + service.getTask().getVersion().name() + "§8]");
        sender.sendMessage("  §8» %1Address: §7" + service.getHostName() + ":" + service.getPort());
        sender.sendMessage("  §8» %1State: " + service.getServiceState().getName());
        sender.sendMessage("  §8» %1Process: " + pcs.getWorkingDirectory() + " ==> " + pcs.getProcess());
        sender.sendMessage("  §8» %1Visibility: §7" + service.getServiceVisibility());
        sender.sendMessage("  §8» %1Players: §7" + service.getOnlinePlayerCount() + "§8/§7" + service.getMaxPlayers());
        sender.sendMessage("  §8» %1Motd: §7" + service.getMotd());
        sender.sendMessage("  §8» %1Ready: §7" + (service.isReady() ? "§aYes" : "§cNo"));
        sender.sendMessage("  §8» %1Uptime: §7" + service.getReadableUptime());
        sender.sendMessage("  §8» %1Last Sync: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp()));
        sender.sendMessage("  §8» %1Would time out at: §7" + new SimpleDateFormat("HH:mm:ss").format(service.getLastCycleData().getTimestamp() + CloudDriver.Constants.SERVER_PUBLISH_INTERVAL));
        sender.sendMessage("  §8» %1Packet Latency: §7" + service.getLastCycleData().getLatency());
        sender.sendMessage("  §8» %1Cycle Data: §7" + service.getLastCycleData().getData().asFormattedJsonString());
        sender.sendMessage("  §8» §8");

    }
}