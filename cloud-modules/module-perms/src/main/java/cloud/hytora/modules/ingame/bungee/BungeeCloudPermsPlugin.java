package cloud.hytora.modules.ingame.bungee;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.modules.ingame.RemotePermissionManager;
import cloud.hytora.modules.ingame.bungee.listener.BungeeCloudPermsListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCloudPermsPlugin extends Plugin {


    @Override
    public void onEnable() {
        this.initListeners();
    }

    private void initListeners() {
        CloudDriver.getInstance().getProviderRegistry().setProvider(PermissionManager.class, new RemotePermissionManager());
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeCloudPermsListener());
    }
}
