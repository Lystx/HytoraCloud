package cloud.hytora.modules.sign.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.impl.SpigotNPCManager;
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


    @Override
    public void onEnable() {
        instance = this;
        new BukkitCloudSignAPI();


        Bukkit.getPluginManager().registerEvents(new PlayerSignListener(), this);

        CloudDriver.getInstance().getCommandManager().registerCommand(new BukkitSignCloudCommand());
        CloudDriver.getInstance().getChannelMessenger().registerChannel(CloudSignAPI.CHANNEL_NAME, new BukkitMessageHandler());


        CloudSignAPI.getInstance().getSignManager().loadCloudSignsSync();

        
        CloudDriver.getInstance().setProvider(NPCManager.class, new SpigotNPCManager());
        CloudDriver.getInstance().setProvider(ICloudSignManager.class, CloudSignAPI.getInstance().getSignManager());
        

        CloudDriver.getInstance().getProvider(NPCManager.class).load(this);

    }

    @Override
    public void onDisable() {
        CloudDriver.getInstance().getCommandManager().unregisterCommand(BukkitSignCloudCommand.class);
        CloudDriver.getInstance().getChannelMessenger().unregisterChannel(CloudSignAPI.CHANNEL_NAME);

        CloudDriver.getInstance().getProvider(NPCManager.class).unload();
    }
}
