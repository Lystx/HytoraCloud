package cloud.hytora.bridge.minecraft.spigot;

import cloud.hytora.bridge.minecraft.BukkitCloudPlayer;
import cloud.hytora.bridge.minecraft.command.ReloadCommand;
import cloud.hytora.bridge.minecraft.command.StopCommand;
import cloud.hytora.bridge.minecraft.spigot.handler.SpigotPacketHandler;
import cloud.hytora.bridge.minecraft.spigot.listener.BukkitPlayerCommandListener;
import cloud.hytora.bridge.minecraft.spigot.handler.SpigotCloudPlayerHandler;
import cloud.hytora.bridge.minecraft.spigot.handler.SpigotNametagHandler;
import cloud.hytora.bridge.minecraft.spigot.listener.BukkitPlayerJoinListener;
import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.PlayerExtension;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.player.extension.CloudProxyPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.ServiceCycleData;
import cloud.hytora.driver.entity.services.impl.DefaultServiceCycleData;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.LoginCheckResult;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.stream.Collectors;

public class SpigotBootstrap extends JavaPlugin implements PluginBridge {


    @Getter
    @Setter
    private int shutdownSchedulerId = -1;

    @Override
    public void onLoad() {
        Remote remote = Remote.init(getIdentity());
        remote.setAdapter(this);



        PlayerExtension extension = CloudDriver.getInstance().getProvider(PlayerExtension.class);
        CloudDriver.getInstance().setProvider(PlayerExtension.class, new PlayerExtension() {
            @Override
            public CloudProxyPlayer createProxyPlayer(CloudPlayer cloudPlayer) {
                return extension.createProxyPlayer(cloudPlayer);
            }

            @Override
            public CloudBukkitPlayer createBukkitPlayer(CloudPlayer cloudPlayer) {
                return new BukkitCloudPlayer(cloudPlayer);
            }
        });
        startStopTimer();
    }

    public void startStopTimer() {

        CloudService currentService = CloudDriver.getInstance().getServiceManager().thisService();
        try {
            if (!currentService.hasProperty("timeOutIfNoPlayers")) {
                return;
            }
            this.shutdownSchedulerId = Bukkit.getScheduler().scheduleSyncDelayedTask(this, () -> {
                if (Bukkit.getOnlinePlayers().size() <= 0) {
                    this.shutdown();
                }
            }, (currentService.getProperties().getLong("timeOutIfNoPlayers") * 20));
        } catch (NullPointerException e) {
            //NullPointerException when executing '/stop'
        }
    }

    @Override
    public void onEnable() {
        this.initialize();

        Bukkit.getPluginManager().registerEvents(new BukkitPlayerCommandListener(), this);
        Bukkit.getPluginManager().registerEvents(new BukkitPlayerJoinListener(this), this);

        CloudDriver.getInstance().getCommandManager().registerCommand(new ReloadCommand());
        CloudDriver.getInstance().getCommandManager().registerCommand(new StopCommand());

        CloudDriver.getInstance().getExecutor().registerPacketHandler(new SpigotNametagHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new SpigotCloudPlayerHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new SpigotPacketHandler());
        Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable(){
            @Override
            public void run(){

                updateServiceInfo();
            }
        });

    }

    @Override
    public void onDisable() {
        this.displayServerInfoStopping();
    }


    @Override
    public Task<Boolean> shutdown() {
        Task<Boolean> shutdown = Task.empty();
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            for (CloudPlayer onlinePlayer : CloudDriver.getInstance().getServiceManager().thisService().getOnlinePlayers()) {
                onlinePlayer.asProxyPlayer().sendToFallback();
            }
        }, 10L);
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            shutdown.setResult(true);
        }, 20L);
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
        }, 25L);
        return shutdown;
    }

    BiSupplier<CloudPlayer, LoginCheckResult> checker;
    @Override
    public void setLoginChecker(BiSupplier<CloudPlayer, LoginCheckResult> checker) {
        this.checker = checker;
    }

    @Override
    public BiSupplier<CloudPlayer, LoginCheckResult> getLoginChecker() {
        return checker;
    }

    @Override
    public void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    @Override
    public ServiceCycleData createCycleData() {
        return new DefaultServiceCycleData(
                Document.gson()
                        .set("players", Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
                        .set("plugins", Arrays.stream(Bukkit.getPluginManager().getPlugins()).map(Plugin::getName).collect(Collectors.toList()))
                        .set("serverName", Bukkit.getServer().getName())
                        .set("version", Bukkit.getServer().getVersion())

        );
    }

}
