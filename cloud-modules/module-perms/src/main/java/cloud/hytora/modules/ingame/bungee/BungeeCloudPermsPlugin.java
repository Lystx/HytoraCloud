package cloud.hytora.modules.ingame.bungee;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.modules.ingame.RemotePermissionManager;
import cloud.hytora.modules.ingame.bungee.listener.BungeeCloudPermsListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCloudPermsPlugin extends Plugin {

    @Override
    public void onEnable() {
        CloudDriver.getInstance().setProvider(PermissionManager.class, new RemotePermissionManager());

        this.initListeners();


    }

    private void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeCloudPermsListener());
    }
}
