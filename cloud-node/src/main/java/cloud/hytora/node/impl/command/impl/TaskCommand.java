package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.context.CommandContext;
import cloud.hytora.driver.commands.data.Command;
import cloud.hytora.driver.commands.data.enums.AllowedCommandSender;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.commands.help.ArgumentHelp;
import cloud.hytora.driver.commands.help.ArgumentHelper;
import cloud.hytora.driver.commands.parameter.CommandArguments;
import cloud.hytora.driver.commands.tabcomplete.TabCompleter;
import cloud.hytora.driver.commands.tabcomplete.TabCompletion;
import cloud.hytora.driver.networking.packets.DriverUpdatePacket;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.task.bundle.DefaultTaskGroup;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.task.UniversalServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateStorage;
import cloud.hytora.driver.services.template.def.CloudTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.setup.TaskSetup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

@Command(
        label = "task",
        aliases = {"tasks"},
        desc = "Manages all tasks",
        invalidUsageIfEmptyInput = true,
        autoHelpAliases = {"help", "?"}
)
public class TaskCommand {

    @ArgumentHelp
    public void onArgumentHelp(ArgumentHelper helper) {
        helper.performTemplateHelp();
    }

    @TabCompletion
    public void onTabComplete(TabCompleter completer) {
        completer.reactWithSubCommands("task");
    }


    @Command(
            label = "info",
            parent = "task",
            usage = "<task>",
            desc = "Gives info about a task",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void infoCommand(CommandContext<?> ctx, CommandArguments args) {

        IServiceTask task = args.get(0, IServiceTask.class);
        if (task == null) {
            ctx.sendMessage("§cThis ServiceTask does not exists");
            return;
        }

        ctx.sendMessage("§8");
        ctx.sendMessage("§bInformation§8: ");
        ctx.sendMessage("§bName: §f" + task.getName());
        ctx.sendMessage("§bTemplates: §f" + task.getTaskGroup().getTemplates().stream().map(ServiceTemplate::getPrefix).collect(Collectors.toList()));
        ctx.sendMessage("§bNode: §f" + task.getPossibleNodes());
        ctx.sendMessage("§bMemory: §f" + task.getMemory() + "MB");
        ctx.sendMessage("§bStartOrder: §f" + task.getStartOrder());
        ctx.sendMessage("§bJava: §f" + task.getJavaVersion());
        ctx.sendMessage("§bPermission: §f" + task.getPermission());
        ctx.sendMessage("§bProperties: §f" + task.getProperties().asRawJsonString());
        ctx.sendMessage("§bMaintenance: §f" + (task.isMaintenance() ? "§aYes" : "§cNo"));
        ctx.sendMessage("§bMin online services: §f" + task.getMinOnlineService());
        ctx.sendMessage("§bServices: §f" + task.getOnlineServices().size() + "/" + (task.getMaxOnlineService() == -1 ? "XXX" : String.valueOf(task.getMaxOnlineService())));
        ctx.sendMessage("§bBehaviour: §f" + task.getTaskGroup().getShutdownBehaviour());
        ctx.sendMessage("§bVersion: §f" + task.getVersion().getTitle());
        ctx.sendMessage("§8");
    }

    @Command(
            label = "create",
            parent = "task",
            desc = "Creates a task",
            scope = CommandScope.CONSOLE
    )
    public void createCommand(CommandContext<?> ctx, CommandArguments args) {

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
                String templateStorage = setup.getTemplateStorage();
                ServiceShutdownBehaviour shutdownBehaviour = dynamic ? ServiceShutdownBehaviour.DELETE : ServiceShutdownBehaviour.KEEP;

                UniversalServiceTask serviceTask = new UniversalServiceTask();

                if (!CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getTaskGroupByName(parentName).isPresent()) {
                    DefaultTaskGroup parent = new DefaultTaskGroup(name, version.getEnvironment(), shutdownBehaviour, new String[]{
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
                    CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).addTaskGroup(parent);
                }

                serviceTask.setName(name);
                serviceTask.setMemory(memory);
                serviceTask.setVersion(version);
                serviceTask.setPossibleNodes(Arrays.asList(nodes));
                serviceTask.setParent(parentName);
                serviceTask.setMaintenance(maintenance);
                serviceTask.setPermission(null);
                serviceTask.setJavaVersion(javaVersion);
                serviceTask.setMotd("Default HytoraCloud Service.");

                if (serviceTask.getTaskGroup().getEnvironment() == SpecificDriverEnvironment.PROXY) {
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

                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).addTask(serviceTask);

                //creating templates
                for (ServiceTemplate template : serviceTask.getTaskGroup().getTemplates()) {
                    TemplateStorage storage = template.getStorage();
                    if (storage != null) {
                        storage.createTemplate(template);
                    }
                }

                ctx.sendMessage("§7The ServiceTask §b" + name + " §7was created§8!");
                NodeDriver.getInstance().getServiceQueue().dequeue();
                DriverUpdatePacket.publishUpdate(CloudDriver.getInstance().getNetworkExecutor());

            } else {
                ctx.sendMessage("§cNo ServiceTask has been created!");
            }
        });
    }

    @Command(
            label = "delete",
            parent = "task",
            usage = "<task>",
            desc = "Deletes a task",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void deleteCommand(CommandContext<?> ctx, CommandArguments args) {
        IServiceTask task = args.get(0, IServiceTask.class);
        if (task == null) {
            ctx.sendMessage("§cThere is no existing ServiceTask with the name §e" + args.get(0) + "§c!");
            return;
        }
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).removeTask(task);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).getAllServicesByTask(task).forEach(ser -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceManager.class).shutdownService(ser));

        ctx.sendMessage("§7The ServiceTask §b" + task.getName() + " §7was deleted§8!");
    }

    @Command(
            label = "toggleMaintenance",
            parent = "task",
            usage = "<task>",
            desc = "Toggles maintenance for a task",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void toggleMaintenanceTask(CommandContext<?> ctx, CommandArguments args) {
        IServiceTask task = args.get(0, IServiceTask.class);
        if (task == null) {
            ctx.sendMessage("§cThere is no existing ServiceTask with the name §e" + args.get(0) + "§c!");
            return;
        }
        boolean maintenance = !task.isMaintenance();
        task.setMaintenance(maintenance);
        task.update();

        ctx.sendMessage("§7The maintenance state of ServiceTask §b" + task.getName() + " §7is now " + (maintenance ? "§aEnabled": "§cDisabled") + "§8!");
    }

    @Command(
            label = "list",
            parent = "task",
            desc = "Lists all tasks",
            scope = CommandScope.CONSOLE_AND_INGAME
    )
    public void listCommand(CommandContext<?> ctx, CommandArguments args) {
        Collection<IServiceTask> cachedTasks = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getAllCachedTasks();
        if (cachedTasks.isEmpty()) {
            ctx.sendMessage("§cThere are no ServiceTasks cached at the moment!");
            return;
        }
        ctx.sendMessage("§8");
        for (IServiceTask g : cachedTasks) {
            ctx.sendMessage("§8=> §b" + g.getName() + " §8(§b" + (g.getVersion().isProxy() ? "PROXY" : "MINECRAFT") + "§8)");
        }
        ctx.sendMessage("§8");
    }
}
