package cloud.hytora.modules.impl;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.gson.adapter.ExcludeJsonField;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

@Getter
@NoArgsConstructor
public class DefaultPermissionPlayer implements PermissionPlayer {


    @Setter @ExcludeJsonField
    private CloudOfflinePlayer offlinePlayer;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

    }

    @Override
    public void addPermission(Permission permission) {

    }

    @Override
    public void removePermission(Permission permission) {

    }

    @Override
    public Collection<Permission> getPermissions() {
        return null;
    }

    @Override
    public Task<Permission> getPermission(String permission) {
        return null;
    }

    @Override
    public Permission getPermissionOrNull(String permission) {
        return null;
    }

    @Override
    public boolean hasPermission(String permission) {
        return false;
    }

    @Override
    public boolean hasPermission(Permission permission) {
        return false;
    }

    @Override
    public void update() {

    }

    @Nullable
    @Override
    public CloudPlayer toOnlinePlayer() {
        return null;
    }

    @NotNull
    @Override
    public CloudOfflinePlayer toOfflinePlayer() {
        return null;
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getPermissionGroups() {
        return null;
    }

    @Nullable
    @Override
    public PermissionGroup getHighestGroup() {
        return null;
    }

    @Override
    public void addPermissionGroup(@NotNull PermissionGroup group) {

    }

    @Override
    public void addPermissionGroup(@NotNull PermissionGroup group, TimeUnit unit, long value) {

    }

    @Override
    public void removePermissionGroup(String groupName) {

    }
}
