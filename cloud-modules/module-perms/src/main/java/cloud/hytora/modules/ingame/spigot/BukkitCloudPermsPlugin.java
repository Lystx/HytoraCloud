package cloud.hytora.modules.ingame.spigot;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.modules.ingame.RemotePermissionManager;
import cloud.hytora.modules.ingame.spigot.listener.BukkitCloudPermsListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitCloudPermsPlugin extends JavaPlugin {


    @Override
    public void onEnable() {
        this.initInjections();
        this.initListeners();
    }

    @Override
    public void onLoad() {
        CloudDriver.getInstance().setProvider(PermissionManager.class, new RemotePermissionManager());
    }

    private void initInjections() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            BukkitCloudPermsHelper.injectPermissible(onlinePlayer);
        }
    }

    private void initListeners() {
        Bukkit.getPluginManager().registerEvents(new BukkitCloudPermsListener(), this);
    }
}
