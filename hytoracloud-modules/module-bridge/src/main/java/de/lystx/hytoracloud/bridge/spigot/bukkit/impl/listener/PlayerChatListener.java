package de.lystx.hytoracloud.bridge.spigot.bukkit.impl.listener;

import de.lystx.hytoracloud.bridge.spigot.bukkit.utils.ConsoleCommandSenderExecutor;
import de.lystx.hytoracloud.driver.CloudDriver;
import de.lystx.hytoracloud.driver.player.permission.impl.PermissionGroup;
import de.lystx.hytoracloud.driver.player.ICloudPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import de.lystx.hytoracloud.driver.utils.interfaces.PlaceHolder;
import org.bukkit.event.server.RemoteServerCommandEvent;

public class PlayerChatListener implements Listener {

    @EventHandler
    public void handle(PlayerCommandPreprocessEvent event) {

        Player player = event.getPlayer();
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedObject(player.getUniqueId());

        String command = event.getMessage().substring(1).split(" ")[0];

        if (CloudDriver.getInstance().getCommandManager().getCommand(command) != null) {
            event.setCancelled(true);
            CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, true, event.getMessage());
        }

    }

    @EventHandler
    public void handle(RemoteServerCommandEvent event) {
        CommandSender sender = event.getSender();

        ConsoleCommandSenderExecutor consoleCommandSender = new ConsoleCommandSenderExecutor(sender);

        String command = event.getCommand().split(" ")[0];

        if (CloudDriver.getInstance().getCommandManager().getCommand(command) != null) {
            event.setCancelled(true);
            CloudDriver.getInstance().getCommandManager().executeCommand(consoleCommandSender, true, event.getCommand());
        }
    }


   @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (CloudDriver.getInstance().getBukkit().shouldUseChat() && CloudDriver.getInstance().getPermissionPool().isAvailable()) {
            event.setCancelled(true);
            String message = event.getMessage();
            PermissionGroup group = CloudDriver.getInstance().getPermissionPool().getHighestPermissionGroup(event.getPlayer().getUniqueId());
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                String chatFormat = ChatColor.translateAlternateColorCodes('&', (group.getChatFormat().trim().isEmpty() ? CloudDriver.getInstance().getBukkit().getChatFormat() : group.getChatFormat()));

                String formatted = PlaceHolder.apply(chatFormat, group).replace("%message%", message).replace("%player%", event.getPlayer().getName());

                onlinePlayer.sendMessage(formatted);
            }
        }
    }

}
