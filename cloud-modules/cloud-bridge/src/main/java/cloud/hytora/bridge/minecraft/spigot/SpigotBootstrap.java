package cloud.hytora.bridge.minecraft.spigot;

import cloud.hytora.bridge.minecraft.spigot.listener.BukkitPlayerCommandListener;
import cloud.hytora.bridge.minecraft.spigot.handler.SpigotCloudPlayerHandler;
import cloud.hytora.bridge.minecraft.spigot.handler.SpigotNametagHandler;
import cloud.hytora.bridge.minecraft.spigot.utils.SpigotLocationFinder;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.executor.PlayerLocationFinder;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.remote.Remote;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotBootstrap extends JavaPlugin implements PluginBridge {

    @Override
    public void onLoad() {
        Remote remote = Remote.init(getIdentity());
        remote.setAdapter(this);


        CloudDriver.getInstance().setProvider(PlayerLocationFinder.class, new SpigotLocationFinder());
    }

    @Override
    public void onEnable() {
        this.initialize();
        this.updateServiceInfo();

        Bukkit.getPluginManager().registerEvents(new BukkitPlayerCommandListener(), this);

        CloudDriver.getInstance().getExecutor().registerPacketHandler(new SpigotNametagHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new SpigotCloudPlayerHandler());
    }

    @Override
    public void onDisable() {
        this.displayServerInfoStopping();
    }


    @Override
    public void shutdown() {
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
        }, 10L);
    }

    @Override
    public void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public IServiceCycleData createCycleData() {
        return new DefaultServiceCycleData(DocumentFactory.newJsonDocument()); // TODO: 01.08.2022 add bukkit info
    }
}
