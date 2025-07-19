package cloud.hytora.modules.sign.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.message.base.ChannelMessage;
import cloud.hytora.driver.common.message.IMessageChannel;
import cloud.hytora.modules.sign.api.CloudSignAPI;
import cloud.hytora.modules.sign.api.ICloudSignManager;
import cloud.hytora.modules.sign.spigot.command.BukkitSignCloudCommand;
import cloud.hytora.modules.sign.spigot.handler.BukkitMessageHandler;
import cloud.hytora.modules.sign.spigot.listener.PlayerSignListener;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public class BukkitBootstrap extends JavaPlugin {

    @Getter
    private static BukkitBootstrap instance;
    private IMessageChannel<ChannelMessage> signChannel;

    @Override
    public void onEnable() {
        instance = this;
        new BukkitCloudSignAPI();


        Bukkit.getPluginManager().registerEvents(new PlayerSignListener(), this);

        CloudDriver.getInstance().getCommandManager().registerCommand(new BukkitSignCloudCommand());
        this.signChannel = CloudDriver.getInstance().getChannelMessenger().registerChannel(ChannelMessage.class, CloudSignAPI.CHANNEL_NAME);
        this.signChannel.registerListener(new BukkitMessageHandler());


        CloudSignAPI.getInstance().getSignManager().loadCloudSignsSync();

        
        CloudDriver.getInstance().setProvider(ICloudSignManager.class, CloudSignAPI.getInstance().getSignManager());
        


    }

    @Override
    public void onDisable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(BukkitSignCloudCommand.class);
        this.signChannel.unregister();

    }
}
