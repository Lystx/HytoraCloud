package cloud.hytora.modules.cloud.listener;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.player.CloudPlayerChangeServerEvent;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginEvent;
import cloud.hytora.driver.event.defaults.server.ServiceClusterConnectEvent;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.message.ChannelMessage;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.cloud.ModulePermissionManager;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsGroupUpdatePacket;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;
import cloud.hytora.modules.global.packets.PermsUpdatePlayerPacket;

public class SyncListener {

    @EventListener
    public void handle(CloudPlayerLoginEvent event) {

        ICloudPlayer cloudPlayer = event.getCloudPlayer();
        PermissionManager permissionManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);
        PermissionPlayer permissionPlayer = permissionManager.getPlayerByUniqueIdOrNull(cloudPlayer.getUniqueId());

        if (permissionPlayer == null) {
            if (permissionManager.hasEntry(cloudPlayer.getUniqueId())) {
                cloudPlayer.executor().disconnect("§cPermsModule: LoginEvent error -> PermsPlayer not found before login!");
            } else {
                permissionPlayer = new DefaultPermissionPlayer(cloudPlayer.getName(), cloudPlayer.getUniqueId());

                for (PermissionGroup allCachedPermissionGroup : permissionManager.getAllCachedPermissionGroups()) {
                    if (allCachedPermissionGroup.isDefaultGroup()) {
                        permissionPlayer.addPermissionGroup(allCachedPermissionGroup);
                    }
                }
                ((ModulePermissionManager) permissionManager).addToCache(permissionPlayer);
                permissionPlayer.update();
                cloudPlayer.sendMessage("§7Deine PermsGroup lautet: §e" + permissionPlayer.getHighestGroup().getName());
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
    public void handle(ServiceClusterConnectEvent event) {
        ICloudService cloudServer = event.getCloudService();

        //updating cache of service

        cloudServer.sendDocument(
                new PermsCacheUpdatePacket(
                        CloudDriver.getInstance()
                                .getProviderRegistry()
                                .getUnchecked(PermissionManager.class)
                                .getAllCachedPermissionGroups()
                )
        );

    }


    @EventListener
    public void handle(CloudPlayerChangeServerEvent event) {
        ICloudService server = event.getServer();
        ICloudPlayer player = event.getPlayer();

        //sending player update to changed server and proxy
        server.sendDocument(new PermsUpdatePlayerPacket(player.asPermissionPlayer()));
        player.getProxyServer().sendDocument(new PermsUpdatePlayerPacket(player.asPermissionPlayer()));

    }
}
