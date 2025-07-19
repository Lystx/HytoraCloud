package cloud.hytora.bridge.minecraft.spigot.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.command.DefaultCommandManager;
import cloud.hytora.driver.command.DriverCommandInfo;
import cloud.hytora.driver.command.CommandScope;
import cloud.hytora.driver.command.annotation.data.RegisteredCommand;
import cloud.hytora.driver.config.INetworkConfig;
import cloud.hytora.driver.config.ISpigotConfig;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerCommand;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;
import org.bukkit.Difficulty;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BukkitPlayerCommandListener implements Listener {


    @EventHandler
    public void handle(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        INetworkConfig config = CloudDriver.getInstance().getConfigManager().getConfig();

        ISpigotConfig spigotConfig = config.getSpigotConfig();
        String joinMessage = spigotConfig.getJoinMessage();
        if (joinMessage.equalsIgnoreCase("none")) {
            event.setJoinMessage(null);
        } else {
            event.setJoinMessage(joinMessage.replace("{player.name}", player.getName()));
        }

        if (spigotConfig.isPeaceful()) {
            player.getWorld().setDifficulty(Difficulty.PEACEFUL);
        }
    }

    @EventHandler
    public void handleWeather(WeatherChangeEvent event) {
        INetworkConfig config = CloudDriver.getInstance().getConfigManager().getConfig();

        ISpigotConfig spigotConfig = config.getSpigotConfig();
        if (spigotConfig.isWeather()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void handleCommand(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/")) {
            return;
        }

        Player player = event.getPlayer();
        CloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());

        if (cloudPlayer == null) {
            return;
        }


        CloudEventPlayerCommand cloudPlayerCommandEvent = CloudDriver.getInstance().getEventManager().callEvent(new CloudEventPlayerCommand(cloudPlayer, event.getMessage(), false));

        if (cloudPlayerCommandEvent.isCancelled()) {
            System.out.println("Event got cancelled");
            event.setCancelled(true);
            return;
        }


        List<DriverCommandInfo> filteredCommands = ((DefaultCommandManager) CloudDriver.getInstance()
                .getCommandManager())
                .getAllCommands()
                .stream()
                .filter(c -> c.getScope().isIngame() && c.getScope() != CommandScope.CONSOLE_AND_INGAME)
                .collect(Collectors.toList());

        String commandLine = event.getMessage().replace("/", "");
        if (commandLine.trim().equalsIgnoreCase("cloud")) {
            commandLine = "cloud help";
        }

        if (commandLine.startsWith("cloud ")) {
            commandLine = commandLine.replace("cloud ", "");
        } else if (commandLine.startsWith("cloud")) {
            commandLine = commandLine.replace("cloud", "");
        }

        String finalCommandLine = commandLine;
        DriverCommandInfo commandInfo = filteredCommands.stream().filter(c -> finalCommandLine.startsWith(c.getPath())).findFirst().orElse(null);
        if (commandInfo != null) {
            event.setCancelled(true);
            if (commandInfo.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {
                PacketCloudEntityPlayer.forPlayerCommandExecute(cloudPlayer.getUniqueId(), commandLine).publish();
                return;
            }
            CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
        } else {
            String command = event.getMessage().substring(1).split(" ")[0];
            RegisteredCommand registeredCommand = CloudDriver.getInstance().getCommandManager().getCommands().stream().filter(c -> Arrays.stream(c.getNames()).anyMatch(s -> s.equalsIgnoreCase(command))).findFirst().orElse(null);
            if (registeredCommand != null) {
                event.setCancelled(true);
                CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
            }
        }


    }

}
