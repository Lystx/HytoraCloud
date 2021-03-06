package de.lystx.hytoracloud.bridge.spigot.bukkit.impl.command;

import de.lystx.hytoracloud.bridge.spigot.bukkit.BukkitBridge;
import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.command.executor.CommandExecutor;
import de.lystx.hytoracloud.driver.command.execution.CommandInfo;
import de.lystx.hytoracloud.driver.player.ICloudPlayer;
import de.lystx.hytoracloud.driver.command.execution.CommandListener;

@CommandInfo(name = "stop", description = "Stops the server", aliases = {"bukkit:stop", "shutdown", "bukkit:shutdown"})
public class StopCommand implements CommandListener {

    @Override
    public void execute(CommandExecutor sender, String[] args) {
        ICloudPlayer player = (ICloudPlayer) sender;
        if (!player.hasPermission("bukkit.command.stop")) {
            player.sendMessage(CloudDriver.getInstance().getPrefix() +  "§cYou aren't allowed to perform this command!");
            return;
        }
        player.sendMessage(CloudDriver.getInstance().getPrefix() + "§7Stopping §c" + CloudDriver.getInstance().getServiceManager().getThisService().getName() + "§8...");
        BukkitBridge.getInstance().shutdown();
    }
}
