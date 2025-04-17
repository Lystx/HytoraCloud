package cloud.hytora.bridge.spigot;

import cloud.hytora.bridge.spigot.listener.BukkitPlayerCommandListener;
import cloud.hytora.bridge.spigot.utils.Nametag;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.AdvancedNetworkExecutor;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.packet.ServiceUpdateNametagsPacket;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class SpigotBootstrap extends JavaPlugin implements PluginBridge, RemoteAdapter {

    @Override
    public void onLoad() {
        RemoteIdentity identity = getIdentity();
        if (identity.getProcessType() == ServiceProcessType.BRIDGE_PLUGIN) {
            Remote remote = new Remote(identity);
            remote.nexCacheUpdate().syncUninterruptedly().get();
        }

        Remote.getInstance().setAdapter(this);
    }

    @Override
    public void onEnable() {
        this.bootstrap();

        Bukkit.getPluginManager().registerEvents(new BukkitPlayerCommandListener(), this);

        AdvancedNetworkExecutor executor = CloudDriver.getInstance().getExecutor();
        executor.registerPacketHandler((PacketHandler<ServiceUpdateNametagsPacket>) (ctx, packet) -> {
            if (packet.getService().equalsIgnoreCase(Remote.getInstance().thisService().getName())) {
                Nametag nametag = new Nametag();

                for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {

                    nametag.updateNameTags(onlinePlayer);
                }
            }
        });
    }

    @Override
    public void onDisable() {
        ICloudService cloudServer = Remote.getInstance().thisService();
        cloudServer.setServiceState(ServiceState.STOPPING);
        cloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
        cloudServer.update();
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
