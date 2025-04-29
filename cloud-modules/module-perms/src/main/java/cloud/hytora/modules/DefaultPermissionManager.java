package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.permission.*;
import cloud.hytora.modules.global.impl.DefaultPermission;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsGroupUpdatePacket;
import cloud.hytora.modules.global.packets.PermsUpdatePlayerPacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class DefaultPermissionManager implements PermissionManager {

    public DefaultPermissionManager() {
        CloudDriver.getInstance().setProvider(PermissionChecker.class, this);

        CloudDriver.getInstance().getChannelMessenger().registerPacketChannel("cloud_module_perms", (Consumer<PermsCacheUpdatePacket>) packet -> {
            CloudDriver.getInstance().getLogger().debug("====> [Packet] Updated whole cache! [" + packet.getPermissionGroups().size() + "]");
            getAllCachedPermissionGroups().clear();
            getAllCachedPermissionGroups().addAll(packet.getPermissionGroups());
        });
        CloudDriver.getInstance().getChannelMessenger().registerPacketChannel("cloud_module_perms", (Consumer<PermsGroupUpdatePacket>) packet -> {
            getAllCachedPermissionGroups().removeIf(g -> g.getName().equalsIgnoreCase(packet.getGroup().getName()));
            getAllCachedPermissionGroups().add(packet.getGroup());
            CloudDriver.getInstance().getLogger().debug("====> [Packet] Cached Group '" + packet.getGroup().getName() + "' !");
            CloudDriver.getInstance().getLogger().debug("=====> Now cache in total: " + CloudDriver.getInstance().getProvider(PermissionManager.class).getAllCachedPermissionGroups().size());

        });
        CloudDriver.getInstance().getChannelMessenger().registerPacketChannel("cloud_module_perms", (Consumer<PermsUpdatePlayerPacket>) packet -> {
            addToCache(packet.getPlayer());

            /*if (CloudDriver.getInstance().getEnvironment() == DriverEnvironment.NODE) {
                packet.publish(); //now sending update to every server
            }*/
            /*CloudDriver.getInstance().getLogger().debug("====> [Packet] Updated whole cache! [" + packet.getGroups().size() + "]");
            getAllCachedPermissionGroups().clear();
            getAllCachedPermissionGroups().addAll(packet.getGroups());*/
            CloudDriver.getInstance().getLogger().debug("====> [Packet] Cached Player '" + packet.getPlayer().getName() + "' !");
            CloudDriver.getInstance().getLogger().debug("=====> Now cache in total: " + CloudDriver.getInstance().getProvider(PermissionManager.class).getAllCachedPermissionPlayers().size());
        });
    }

    @Override
    public boolean hasPermission(UUID playerUniqueId, String permission) {
        PermissionPlayer p = getPlayerByUniqueIdOrNull(playerUniqueId);
        if (p == null) {
            return false;
        }
        return p.hasPermission("*") || p.hasPermission(permission);
    }

    public abstract void addToCache(PermissionPlayer player);

    @NotNull
    @Override
    public Permission createPermission(@NotNull String permission, @NotNull long expirationDate) {
        return new DefaultPermission(permission, expirationDate);
    }

    @Override
    public PermissionPlayer createPlayer(String name, UUID uniqueId) {
        DefaultPermissionPlayer permissionPlayer = new DefaultPermissionPlayer(name, uniqueId);

        for (PermissionGroup allCachedPermissionGroup : this.getAllCachedPermissionGroups()) {
            if  (allCachedPermissionGroup.isDefaultGroup()) {
                permissionPlayer.addPermissionGroup(allCachedPermissionGroup);
            }
        }
        return permissionPlayer;
    }

    @NotNull
    @Override
    public PermissionGroup createPermissionGroup(@NotNull String name) {
        return new DefaultPermissionGroup(name, "", "", "", "", 1, false, new ArrayList<>(), new HashMap<>());
    }
}
