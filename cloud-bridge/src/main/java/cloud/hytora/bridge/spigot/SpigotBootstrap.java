package cloud.hytora.bridge.spigot;

import cloud.hytora.driver.services.ServiceInfo;
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
        Remote.getInstance().setAdapter(this);
    }

    @Override
    public void onEnable() {
        this.bootstrap();
    }

    @Override
    public void onDisable() {
        ServiceInfo serviceInfo = Remote.getInstance().thisService();
        serviceInfo.setServiceState(ServiceState.STOPPING);
        serviceInfo.setServiceVisibility(ServiceVisibility.INVISIBLE);
        serviceInfo.update();
    }

    @Override
    public void shutdown() {
        Bukkit.getScheduler().runTask(this, Bukkit::shutdown);
    }

    @Override
    public void executeCommand(String command) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }
}
