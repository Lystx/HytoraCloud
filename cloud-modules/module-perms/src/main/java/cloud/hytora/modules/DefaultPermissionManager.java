package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.PlayerFullJoinExecutor;
import cloud.hytora.driver.module.permission.*;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.modules.cloud.handler.GroupPacketHandler;
import cloud.hytora.modules.global.impl.DefaultPermission;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsPlayerUpdatePacket;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public abstract class DefaultPermissionManager implements PermissionManager {

    public DefaultPermissionManager() {
        CloudDriver.getInstance().setProvider(PermissionChecker.class, this);

        CloudDriver.getInstance()

                .setProvider(PlayerFullJoinExecutor.Checker.class, (t1, t2) -> {
                    PermissionPlayer player_A = t1.asPermissionPlayer();
                    PermissionPlayer player_B = t2.asPermissionPlayer();

                    PermissionGroup highestGroup_A = player_A.getHighestGroup();
                    PermissionGroup highestGroup_B = player_B.getHighestGroup();

                    if (highestGroup_A.getSortId() < highestGroup_B.getSortId()) {
                        return t1;
                    } else {
                        return t2;
                    }
                });
        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<PermsCacheUpdatePacket>) (wrapper, packet) -> {
            PacketBuffer buffer = packet.buffer();
            getAllCachedPermissionGroups().clear();
            Collection<PermissionGroup> cachedPermissionGroups = buffer.readWrapperObjectCollection(DefaultPermissionGroup.class);
            getAllCachedPermissionGroups().addAll(cachedPermissionGroups);

            CloudDriver.getInstance().getLogger().info("====> [Packet] Updated whole cache! [" + cachedPermissionGroups.size() + "]");

        });
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new GroupPacketHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler((PacketHandler<PermsPlayerUpdatePacket>) (wrapper, packet) -> {
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
        PermissionPlayer p = getPermissionPlayer(playerUniqueId);
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
    public @NotNull PermissionPlayer createPlayer(String name, UUID uniqueId) {
        DefaultPermissionPlayer permissionPlayer = new DefaultPermissionPlayer(name, uniqueId);

        for (PermissionGroup allCachedPermissionGroup : this.getAllCachedPermissionGroups()) {
            if (allCachedPermissionGroup.isDefaultGroup()) {
                permissionPlayer.addPermissionGroup(allCachedPermissionGroup);
            }
        }
        return permissionPlayer;
    }

    @NotNull
    @Override
    public PermissionGroup createPermissionGroup(@NotNull String name) {
        return new DefaultPermissionGroup(name, "", "", "", 1, false, new ArrayList<>(), new HashMap<>());
    }
}
