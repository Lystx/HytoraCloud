package cloud.hytora.modules.cloud.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerLogin;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceReady;
import cloud.hytora.driver.event.listener.EventListener;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerChangeServer;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerLoginSuccess;
import cloud.hytora.driver.event.defaults.server.CloudEventServiceConnect;
import cloud.hytora.driver.module.permission.PermissionGroup;
import cloud.hytora.driver.module.permission.PermissionManager;
import cloud.hytora.driver.module.permission.PermissionPlayer;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.modules.cloud.ModulePermissionManager;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;

import java.util.UUID;

public class SyncListener {

    @EventListener
    public void handle(CloudEventPlayerLoginSuccess event) {

        CloudPlayer cloudPlayer = event.getCloudPlayer();
        PermissionManager permissionManager = CloudDriver.getInstance().getProvider(PermissionManager.class);
        PermissionPlayer permissionPlayer = permissionManager.getPermissionPlayer(cloudPlayer.getUniqueId());

        if (permissionPlayer == null) {
            if (permissionManager.hasEntry(cloudPlayer.getUniqueId())) {
                cloudPlayer.asProxyPlayer().disconnect("§cPermsModule: LoginEvent error -> PermsPlayer not found before login!");
            } else {
                permissionPlayer = new DefaultPermissionPlayer(cloudPlayer.getName(), cloudPlayer.getUniqueId());

                for (PermissionGroup allCachedPermissionGroup : permissionManager.getAllCachedPermissionGroups()) {
                    if (allCachedPermissionGroup.isDefaultGroup()) {
                        permissionPlayer.addPermissionGroup(allCachedPermissionGroup);
                    }
                }
                ((ModulePermissionManager) permissionManager).addToCache(permissionPlayer);
                permissionPlayer.update();
            }
        } else if (permissionPlayer.getHighestGroup() == null) {

            for (PermissionGroup allCachedPermissionGroup : permissionManager.getAllCachedPermissionGroups()) {
                if (allCachedPermissionGroup.isDefaultGroup()) {
                    permissionPlayer.addPermissionGroup(allCachedPermissionGroup);
                }
            }
            ((ModulePermissionManager) permissionManager).addToCache(permissionPlayer);
            permissionPlayer.update();
        } else {
            PermissionPlayer finalPermissionPlayer = permissionPlayer;
            if (permissionManager.getAllCachedPermissionPlayers().stream().noneMatch(p -> p.getUniqueId().equals(finalPermissionPlayer.getUniqueId()))) {
                ((ModulePermissionManager) permissionManager).addToCache(permissionPlayer);
                permissionPlayer.update();
            }
        }
    }

    @EventListener
    public void handle(CloudEventServiceReady event) {
        CloudService cloudServer = event.getCloudServer();

        //updating cache of service

        cloudServer.sendPacket(
                new PermsCacheUpdatePacket(
                        CloudDriver.getInstance()
                                
                                .getProvider(PermissionManager.class)
                                .getAllCachedPermissionGroups()
                )
        );

    }

    @EventListener
    public void handle(CloudEventPlayerLogin event) {
        PlayerConnection connection = event.getConnection();
        UUID playerId = connection.getConnectionId();

        CloudService firstJoinServer = event.getFirstJoinServer();

        firstJoinServer.sendPacket(new PermsPlayerUpdatePacket(CloudDriver.getInstance().getProvider(PermissionManager.class).getPermissionPlayer(playerId)));
    }


    @EventListener
    public void handle(CloudEventPlayerChangeServer event) {
        CloudService server = event.getServer();
        CloudPlayer player = event.getPlayer();

        //sending player update to changed server and proxy
        server.sendPacket(new PermsPlayerUpdatePacket(player.asPermissionPlayer()));
        player.getProxyServer().sendPacket(new PermsPlayerUpdatePacket(player.asPermissionPlayer()));

    }
}
