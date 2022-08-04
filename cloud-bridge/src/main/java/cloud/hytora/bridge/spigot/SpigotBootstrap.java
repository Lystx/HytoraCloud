package cloud.hytora.bridge.spigot;

import cloud.hytora.bridge.spigot.listener.BukkitPlayerCommandListener;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.IServiceCycleData;
import cloud.hytora.driver.services.impl.DefaultServiceCycleData;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceProcessType;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.bridge.PluginBridge;
import cloud.hytora.remote.Remote;
import cloud.hytora.remote.adapter.RemoteAdapter;
import org.bukkit.Bukkit;
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
    }

    @Override
    public void onDisable() {
        ICloudServer cloudServer = Remote.getInstance().thisService();
        cloudServer.setServiceState(ServiceState.STOPPING);
        cloudServer.setServiceVisibility(ServiceVisibility.INVISIBLE);
        cloudServer.update();
    }

    @Override
    public void shutdown() {
        Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
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
