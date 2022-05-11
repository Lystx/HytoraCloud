package cloud.hytora.node.impl.command.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.*;
import cloud.hytora.driver.command.sender.CommandSender;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.fallback.SimpleFallback;
import cloud.hytora.driver.services.configuration.SimpleServerConfiguration;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.driver.setup.SetupControlState;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.impl.setup.ConfigurationSetup;

import java.util.List;

@Command(
        name = {"config", "configurations", "con"},
        scope = CommandScope.CONSOLE_AND_INGAME,
        permission = "cloud.command.use"
)
@CommandDescription("Manages all service configurations")
@CommandAutoHelp
public class ConfigurationCommand {

    
    @SubCommand("info <name>")
    @CommandDescription("Shows info about a configuration")
    public void execute(CommandSender sender, @CommandArgument("name") String name) {

        ServerConfiguration configuration = CloudDriver.getInstance().getConfigurationManager().getConfigurationByNameOrNull(name);

        if (configuration == null) {
            sender.sendMessage("§cThis configuration does not exists");
            return;
        }

        sender.sendMessage("§8");
        sender.sendMessage("§bInformation§8: ");
        sender.sendMessage("§bName: §f" + configuration.getName());
        sender.sendMessage("§bTemplate: §f" + configuration.getTemplate());
        sender.sendMessage("§bNode: §f" + configuration.getNode());
        sender.sendMessage("§bMemory: §f" + configuration.getMemory() + "MB");
        sender.sendMessage("§bMin online services: §f" + configuration.getMinOnlineService());
        sender.sendMessage("§bServices: §f" + configuration.getOnlineServices().size() + "/" + (configuration.getMaxOnlineService() == -1 ? "Unlimited" : String.valueOf(configuration.getMaxOnlineService())));
        sender.sendMessage("§bBehaviour: §f" + configuration.getShutdownBehaviour());
        sender.sendMessage("§bVersion: §f" + configuration.getVersion().getTitle());
        sender.sendMessage("§8");
    }
    @SubCommand("create")
    @CommandDescription("Creates a new configuration")
    public void executeCreate(CommandSender sender) {

        new ConfigurationSetup().start((setup, state) -> {
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

                SimpleServerConfiguration configuration = new SimpleServerConfiguration();

                configuration.setName(name);
                configuration.setMemory(memory);
                configuration.setVersion(version);
                configuration.setNode(node);
                configuration.setMaintenance(maintenance);
                configuration.setTemplate("default");
                configuration.setJavaVersion(javaVersion);
                configuration.setMotd("Please configure me!");

                SimpleFallback fallback = new SimpleFallback();
                fallback.setEnabled(setup.isFallback());
                if (setup.isFallback()) {
                    fallback.setPermission(setup.getFallbackPermission().equalsIgnoreCase("none") ? "" : setup.getFallbackPermission());
                    fallback.setPriority(setup.getFallbackPriority());
                } else {
                    fallback.setPriority(1);
                    fallback.setPermission("");
                }

                configuration.setFallback(fallback);
                configuration.setStartOrder(1);
                configuration.setShutdownBehaviour(dynamic ? ServiceShutdownBehaviour.DELETE : ServiceShutdownBehaviour.KEEP);
                configuration.setDefaultMaxPlayers(maxPlayers);
                configuration.setMinOnlineService(minServers);
                configuration.setMaxOnlineService(maxServers);

                CloudDriver.getInstance().getConfigurationManager().addConfiguration(configuration);
                NodeDriver.getInstance().getNodeTemplateService().createTemplateFolder(configuration);

                sender.sendMessage("§7The configuration §b" + name + " §7was created§8!");

                NodeDriver.getInstance().getServiceQueue().dequeue();

            } else {
                sender.sendMessage("§cNo configuration has been created!");
            }
        });
    }

    @SubCommand("delete <name>")
    @CommandDescription("Deletes a configuration")
    public void executeDelete(CommandSender sender, @CommandArgument("name") String name) {
        ServerConfiguration configuration = CloudDriver.getInstance().getConfigurationManager().getConfigurationByNameOrNull(name);
        if (configuration == null) {
            sender.sendMessage("§cThere is no existing configuration with the name §e" + name + "§c!");
            return;
        }
        CloudDriver.getInstance().getConfigurationManager().removeConfiguration(configuration);
        CloudDriver.getInstance().getServiceManager().getAllServicesByGroup(configuration)
                .forEach(it -> CloudDriver.getInstance().getServiceManager().shutdownService(it));

        sender.sendMessage("§7The configuration §b" + configuration.getName() + " §7was deleted§8!");
    }

    @SubCommand("list")
    @CommandDescription("Lists all configurations")
    public void executeList(CommandSender sender) {
        List<ServerConfiguration> cachedConfigurations = CloudDriver.getInstance().getConfigurationManager().getAllCachedConfigurations();
        if (cachedConfigurations.isEmpty()) {
            sender.sendMessage("§cThere are no configuration cached at the moment!");
            return;
        }
        sender.sendMessage("§8");
        for (ServerConfiguration g : cachedConfigurations) {
            sender.sendMessage("§8=> §b" + g.getName() + " §8(§b" + (g.getVersion().isProxy() ? "Proxy" : "Spigot") + "§8)");
        }
        sender.sendMessage("§8");
    }
}
