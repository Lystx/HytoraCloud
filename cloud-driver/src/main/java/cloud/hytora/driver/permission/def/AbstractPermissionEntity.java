package cloud.hytora.driver.permission.def;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionEntity;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public abstract class AbstractPermissionEntity implements PermissionEntity {

    /**
     * All cached permissions
     */
    private final Collection<Permission> permissions;

    public AbstractPermissionEntity() {
        this.permissions = new ArrayList<>();
    }

    @Override
    public void addPermission(Permission permission) {
        if (!this.hasPermission(permission)) {
            this.permissions.add(permission);
        }
    }

    @Override
    public void removePermission(Permission permission) {
        if (this.hasPermission(permission)) {
            this.permissions.remove(permission);
        }
    }

    @Override
    public Task<Permission> getPermission(String permission) {
        return Task.build(
                this.getPermissions()
                        .stream()
                        .filter(p ->
                                p.getPermission()
                                        .equalsIgnoreCase(permission)
                                        && !p.hasExpired()
                        )
                        .findFirst()
                        .orElse(null)
        );
    }

    @Override
    public Permission getPermissionOrNull(String permission) {
        return getPermission(permission).orNull();
    }

    @Override
    public boolean hasPermission(String permission) {
        Task<Permission> p = getPermission(permission);
        return p.isPresent() && hasPermission(p.get());
    }

}
