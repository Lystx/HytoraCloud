package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.completer.TaskCompleter;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.task.DefaultServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.setup.TaskSetup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Command(
        name = {"task", "tasks", "t"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandDescription("Manages all service tasks")
@CommandAutoHelp
public class TaskCommand {

    
    @SubCommand("info <name>")
    @CommandDescription("Shows info about a task")
    public void execute(CommandSender sender, @CommandArgument(value = "name", completer = TaskCompleter.class) String name) {

        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(name);

        if (task == null) {
            sender.sendMessage("§cThis ServiceTask does not exists");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§bInformation§8: ");
        sender.sendMessage("§bName: §f" + task.getName());
        sender.sendMessage("§bTemplates: §f" + task.getTaskGroup().getTemplates().stream().map(ServiceTemplate::getPrefix).collect(Collectors.toList()));
        sender.sendMessage("§bNode: §f" + task.getNode());
        sender.sendMessage("§bMemory: §f" + task.getMemory() + "MB");
        sender.sendMessage("§bStartOrder: §f" + task.getStartOrder());
        sender.sendMessage("§bJava: §f" + task.getJavaVersion());
        sender.sendMessage("§bPermission: §f" + task.getPermission());
        sender.sendMessage("§bProperties: §f" + task.getProperties().asRawJsonString());
        sender.sendMessage("§bMaintenance: §f" + (task.isMaintenance() ? "§aYes" : "§cNo"));
        sender.sendMessage("§bMin online services: §f" + task.getMinOnlineService());
        sender.sendMessage("§bServices: §f" + task.getOnlineServices().size() + "/" + (task.getMaxOnlineService() == -1 ? "XXX" : String.valueOf(task.getMaxOnlineService())));
        sender.sendMessage("§bBehaviour: §f" + task.getTaskGroup().getShutdownBehaviour());
        sender.sendMessage("§bVersion: §f" + task.getVersion().getTitle());
        sender.sendMessage("§8");
    }
    @SubCommand("create")
    @CommandDescription("Creates a new task")
    public void executeCreate(CommandSender sender) {

        new TaskSetup().start((setup, state) -> {
            if (state == SetupControlState.FINISHED) {
                String name = setup.getName();
                int memory = setup.getMemory();
                int maxPlayers = setup.getMaxPlayers();
                int maxServers = setup.getMaxServers();
                boolean dynamic = setup.isDynamic();
                ServiceVersion version = setup.getVersion();
                int minServers = setup.getMinServers();
                String node = setup.getNode();
                boolean maintenance = setup.isMaintenance();
                int javaVersion = setup.getJavaVersion();
                String parentName = setup.getParentName();
                String templateStorage = setup.getTemplateStorage();
                ServiceShutdownBehaviour shutdownBehaviour = dynamic ? ServiceShutdownBehaviour.DELETE : ServiceShutdownBehaviour.KEEP;

                DefaultServiceTask serviceTask = new DefaultServiceTask();

                if (!CloudDriver.getInstance().getServiceTaskManager().getTaskGroupByName(parentName).isPresent()) {
                    DefaultTaskGroup parent = new DefaultTaskGroup(name, version.getWrapperEnvironment(), shutdownBehaviour, new String[]{
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
                serviceTask.setNode(node);
                serviceTask.setParent(parentName);
                serviceTask.setMaintenance(maintenance);
                serviceTask.setPermission(null);
                serviceTask.setJavaVersion(javaVersion);
                serviceTask.setMotd("Default HytoraCloud Service.");

                if (serviceTask.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY_SERVER) {
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

                serviceTask.setFallback(fallback);
                serviceTask.setStartOrder(1);
                serviceTask.setDefaultMaxPlayers(maxPlayers);
                serviceTask.setMinOnlineService(minServers);
                serviceTask.setMaxOnlineService(maxServers);

                CloudDriver.getInstance().getServiceTaskManager().addTask(serviceTask);

                //creating templates
                for (ServiceTemplate template : serviceTask.getTaskGroup().getTemplates()) {
                    TemplateStorage storage = template.getStorage();
                    if (storage != null) {
                        storage.createTemplate(template);
                    }
                }

                sender.sendMessage("§7The ServiceTask §b" + name + " §7was created§8!");
                NodeDriver.getInstance().getServiceQueue().dequeue();

            } else {
                sender.sendMessage("§cNo ServiceTask has been created!");
            }
        });
    }

    @SubCommand("delete <name>")
    @CommandDescription("Deletes a task")
    public void executeDelete(CommandSender sender, @CommandArgument(value = "name", completer = TaskCompleter.class) String name) {
        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(name);
        if (task == null) {
            sender.sendMessage("§cThere is no existing ServiceTask with the name §e" + name + "§c!");
            return;
        }
        CloudDriver.getInstance().getServiceTaskManager().removeTask(task);
        CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(task).forEach(ser -> CloudDriver.getInstance().getServiceManager().shutdownService(ser));

        sender.sendMessage("§7The ServiceTask §b" + task.getName() + " §7was deleted§8!");
    }

    @SubCommand("toggleMaintenance <name>")
    @CommandDescription("Toggles maintenance mode for a task")
    public void executeToggleMaintenance(CommandSender sender, @CommandArgument(value = "name", completer = TaskCompleter.class) String name) {
        ServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getTaskByNameOrNull(name);
        if (task == null) {
            sender.sendMessage("§cThere is no existing ServiceTask with the name §e" + name + "§c!");
            return;
        }
        boolean maintenance = !task.isMaintenance();
        task.setMaintenance(maintenance);
        task.update();

        sender.sendMessage("§7The maintenance state of ServiceTask §b" + task.getName() + " §7is now " + (maintenance ? "§aEnabled": "§cDisabled") + "§8!");
    }

    @SubCommand("list")
    @CommandDescription("Lists all configurations")
    public void executeList(CommandSender sender) {
        Collection<ServiceTask> cachedTasks = CloudDriver.getInstance().getServiceTaskManager().getAllCachedTasks();
        if (cachedTasks.isEmpty()) {
            sender.sendMessage("§cThere are no ServiceTasks cached at the moment!");
            return;
        }
        sender.sendMessage("§8");
        for (ServiceTask g : cachedTasks) {
            sender.sendMessage("§8=> §b" + g.getName() + " §8(§b" + (g.getVersion().isProxy() ? "PROXY_SERVER" : "MINECRAFT_SERVER") + "§8)");
        }
        sender.sendMessage("§8");
    }
}
