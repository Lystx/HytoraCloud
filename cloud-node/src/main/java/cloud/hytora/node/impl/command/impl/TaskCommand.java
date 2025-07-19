package cloud.hytora.node.impl.command.impl;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.impl.TaskCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.entity.services.task.bundle.TaskGroup;
import cloud.hytora.driver.networking.packets.cache.PacketDriverCacheUpdate;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.entity.services.fallback.SimpleFallback;
import cloud.hytora.driver.entity.services.task.UniversalServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import cloud.hytora.driver.entity.services.template.def.CloudTemplate;
import cloud.hytora.driver.entity.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.common.setup.SetupControlState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.setup.TaskSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Command(
        value = {"task", "tasks"},
        permission = "cloud.hytora.command.use",
        executionScope = CommandScope.CONSOLE_AND_INGAME,
        description = "Manages all service tasks"
)
@Command.AutoHelp

public class TaskCommand {

    
    @Command(value = "info", description = "Shows info about a task")
    @Command.Syntax("<name>")
    public void execute(CommandSender sender, @Command.Argument(value = "name", completer = TaskCompleter.class) String name) {

        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(name);

        if (task == null) {
            sender.sendMessage("§cThis ServiceTask does not exists");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("%1Information§8: ");
        sender.sendMessage("  §8» %1Name: §f" + task.getName());
        sender.sendMessage("  §8» %1Templates: §f" + task.getTaskGroup().getTemplates().stream().map(ServiceTemplate::getPrefix).collect(Collectors.toList()));
        sender.sendMessage("  §8» %1Node: §f" + task.getPossibleNodes());
        sender.sendMessage("  §8» %1Memory: §f" + task.getMemory() + "MB");
        sender.sendMessage("  §8» %1StartOrder: §f" + task.getStartOrder());
        sender.sendMessage("  §8» %1Java: §f" + task.getJavaVersion());
        sender.sendMessage("  §8» %1Permission: §f" + task.getPermission());
        sender.sendMessage("  §8» %1Properties: §f" + task.getProperties().asRawJsonString());
        sender.sendMessage("  §8» %1Maintenance: §f" + (task.isMaintenance() ? "§aYes" : "§cNo"));
        sender.sendMessage("  §8» %1Min online services: §f" + task.getMinOnlineService());
        sender.sendMessage("  §8» %1Services: §f" + task.getOnlineServices().size() + "/" + (task.getMaxOnlineService() == -1 ? "XXX" : String.valueOf(task.getMaxOnlineService())));
        sender.sendMessage("  §8» %1Behaviour: §f" + task.getTaskGroup().getShutdownBehaviour());
        sender.sendMessage("  §8» %1Version: §f" + task.getVersion().getTitle());
        sender.sendMessage("§8");
    }
    @Command(value = "create", description = "Creates a new task")
    public void executeCreate(CommandSender sender) {

        new TaskSetup(NodeDriver.getInstance().getConsole()).start((setup, state) -> {
            if (state == SetupControlState.FINISHED) {
                String name = setup.getName();
                int memory = setup.getMemory();
                int maxPlayers = setup.getMaxPlayers();
                int maxServers = setup.getMaxServers();
                boolean dynamic = setup.isDynamic();
                ServiceVersion version = setup.getVersion();
                int minServers = setup.getMinServers();
                String node = setup.getNode();
                String[] nodes = node.split(",");
                boolean maintenance = setup.isMaintenance();
                int javaVersion = setup.getJavaVersion();
                String parentName = setup.getParentName();
                int percentForNewServer = setup.getPercentForNewServer();
                String templateStorage = setup.getTemplateStorage();
                ServiceShutdownBehaviour shutdownBehaviour = dynamic ? ServiceShutdownBehaviour.DELETE : ServiceShutdownBehaviour.KEEP;

                UniversalServiceTask serviceTask = new UniversalServiceTask();

                if (CloudDriver.getInstance().getServiceTaskManager().getCachedTaskGroup(parentName) == null) {
                    DefaultTaskGroup parent = new DefaultTaskGroup(parentName, version.getEnvironment(), shutdownBehaviour, new String[]{
                            "-XX:+UseG1GC",
                            "-XX:+ParallelRefProcEnabled",
                            "-XX:MaxGCPauseMillis=200",
                            "-XX:+UnlockExperimentalVMOptions",
                            "-XX:+DisableExplicitGC",
                            "-XX:+AlwaysPreTouch",
                            "-XX:G1NewSizePercent=30",
                            "-XX:G1MaxNewSizePercent=40",
                            "-XX:G1HeapRegionSize=8M",
                            "-XX:G1ReservePercent=20",
                            "-XX:G1HeapWastePercent=5",
                            "-XX:G1MixedGCCountTarget=4",
                            "-XX:InitiatingHeapOccupancyPercent=15",
                            "-XX:G1MixedGCLiveThresholdPercent=90",
                            "-XX:G1RSetUpdatingPauseTimePercent=5",
                            "-XX:SurvivorRatio=32",
                            "-XX:+PerfDisableSharedMem",
                            "-XX:MaxTenuringThreshold=1",
                            "-Dusing.aikars.flags=https://mcflags.emc.gs",
                            "-Daikars.new.flags=true",
                            "-XX:-UseAdaptiveSizePolicy",
                            "-XX:CompileThreshold=100",
                            "-Dio.netty.recycler.maxCapacity=0",
                            "-Dio.netty.recycler.maxCapacity.default=0",
                            "-Djline.terminal=jline.UnsupportedTerminal"
                    }, new ArrayList<>(), Collections.singleton(new CloudTemplate(name, "default", templateStorage, true)));
                    CloudDriver.getInstance().getServiceTaskManager().addTaskGroup(parent);
                }

                serviceTask.setName(name);
                serviceTask.setMemory(memory);
                serviceTask.setVersion(version);
                serviceTask.setPossibleNodes(Arrays.asList(nodes));
                serviceTask.setParent(parentName);
                serviceTask.setMaintenance(maintenance);
                serviceTask.setPermission(null);
                serviceTask.setJavaVersion(javaVersion);
                serviceTask.setPercentForNewServer(percentForNewServer);
                serviceTask.setMotd("Default HytoraCloud Service.");


                TaskGroup taskGroup = serviceTask.getTaskGroup();


                if (taskGroup.getEnvironment() == SpecificDriverEnvironment.PROXY) {
                    serviceTask.setProperty("onlineMode", true);
                    serviceTask.setProperty("proxyProtocol", false);
                } else {
                    serviceTask.setProperty("gameServer", true);
                }

                SimpleFallback fallback = new SimpleFallback();
                fallback.setEnabled(setup.isFallback());
                if (setup.isFallback()) {
                    fallback.setPermission(setup.getFallbackPermission().equalsIgnoreCase("none") ? "" : setup.getFallbackPermission());
                    fallback.setPriority(setup.getFallbackPriority());
                } else {
                    fallback.setPriority(1);
                    fallback.setPermission("");
                }

                serviceTask.setStartOrder(setup.getStartOrder());
                serviceTask.setFallback(fallback);
                serviceTask.setDefaultMaxPlayers(maxPlayers);
                serviceTask.setMinOnlineService(minServers);
                serviceTask.setMaxOnlineService(maxServers);

                CloudDriver.getInstance().getServiceTaskManager().addTask(serviceTask);

                //creating templates
                for (ServiceTemplate template : taskGroup.getTemplates()) {
                    TemplateStorage storage = template.getStorage();
                    if (storage != null) {
                        storage.createTemplate(template);
                    }
                }

                sender.sendMessage("§7The ServiceTask %1" + name + " §7was created§8!");
                NodeDriver.getInstance().getServiceQueue().dequeue();
                PacketDriverCacheUpdate.publishUpdate(CloudDriver.getInstance().getExecutor());

            } else {
                sender.sendMessage("§cNo ServiceTask has been created!");
            }
        });
    }

    @Command(value = "delete", description = "Deletes a task")
    @Command.Syntax("<name>")
    public void executeDelete(CommandSender sender, @Command.Argument(value = "name", completer = TaskCompleter.class) String name) {
        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(name);
        if (task == null) {
            sender.sendMessage("§cThere is no existing ServiceTask with the name §e" + name + "§c!");
            return;
        }
        CloudDriver.getInstance().getServiceTaskManager().removeTask(task);
        CloudDriver.getInstance().getServiceManager().getAllServicesByTask(task).forEach(ser -> CloudDriver.getInstance().getServiceManager().shutdownService(ser));

        sender.sendMessage("§7The ServiceTask %1" + task.getName() + " §7was deleted§8!");
    }

    @Command(value = "toggleMaintenance", description = "Toggles maintenance mode for a task")
    @Command.Syntax("<name>")
    public void executeToggleMaintenance(CommandSender sender, @Command.Argument(value = "name", completer = TaskCompleter.class) String name) {
        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(name);
        if (task == null) {
            sender.sendMessage("§cThere is no existing ServiceTask with the name §e" + name + "§c!");
            return;
        }
        boolean maintenance = !task.isMaintenance();
        task.setMaintenance(maintenance);
        task.update();

        sender.sendMessage("§7The maintenance state of ServiceTask %1" + task.getName() + " §7is now " + (maintenance ? "§aEnabled": "§cDisabled") + "§8!");
    }

    @Command(value = "list", description = "Lists all configurations")
    public void executeList(CommandSender sender) {
        Collection<ServiceTask> cachedTasks = CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks();
        if (cachedTasks.isEmpty()) {
            sender.sendMessage("§cThere are no ServiceTasks cached at the moment!");
            return;
        }
        sender.sendMessage("§8");
        for (ServiceTask g : cachedTasks) {
            sender.sendMessage("§8=> %1" + g.getName() + " §8(%1" + (g.getVersion().isProxy() ? "PROXY" : "MINECRAFT") + "§8)");
        }
        sender.sendMessage("§8");
    }
}
