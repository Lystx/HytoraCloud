package cloud.hytora.modules.ingame.bungee;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.PlayerFullJoinChecker;
import cloud.hytora.modules.ingame.RemotePermissionManager;
import cloud.hytora.modules.ingame.bungee.listener.BungeeCloudPermsListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeeCloudPermsPlugin extends Plugin {

    @Override
    public void onEnable() {
        CloudDriver.getInstance().setProvider(PermissionManager.class, new RemotePermissionManager());

        this.initListeners();


        CloudDriver.getInstance()
                
                .setProvider(PlayerFullJoinChecker.class, new PlayerFullJoinChecker() {
                    @Override
                    public ICloudPlayer compare(ICloudPlayer t1, ICloudPlayer t2) {
                        PermissionPlayer player1 = t1.asPermissionPlayer();
                        PermissionPlayer player = t2.asPermissionPlayer();

                        // TODO: 22.04.2025 check fulljoin
                        return t1;
                    }
                });
    }

    private void initListeners() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new BungeeCloudPermsListener());
    }
}
