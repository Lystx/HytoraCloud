package cloud.hytora.modules.sign.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.spigot.command.BukkitSignCloudCommand;
import cloud.hytora.modules.sign.spigot.handler.BukkitMessageHandler;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class BukkitBootstrap extends JavaPlugin {

    @Getter
    private static BukkitBootstrap instance;

    @Override
    public void onEnable() {
        instance = this;
        new BukkitCloudSignAPI();

        CloudDriver.getInstance().getCommandManager().registerCommand(new BukkitSignCloudCommand());
        CloudDriver.getInstance().getChannelMessenger().registerChannel(CloudSignAPI.CHANNEL_NAME, new BukkitMessageHandler());
    }

    @Override
    public void onDisable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(BukkitSignCloudCommand.class);
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(CloudSignAPI.CHANNEL_NAME);
    }
}
