package cloud.hytora.modules.cloud;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.DatabaseSection;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.modules.DefaultPermissionManager;
import cloud.hytora.modules.impl.DefaultPermissionGroup;
import cloud.hytora.modules.impl.DefaultPermissionPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Getter
public class ModulePermissionManager extends DefaultPermissionManager {

    private final List<PermissionGroup> cachedPermissionGroups;

    public ModulePermissionManager() {
        this.cachedPermissionGroups = new ArrayList<>();

        this.loadGroups();
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getAllCachedPermissionGroups() {
        return cachedPermissionGroups;
    }

    private void loadGroups() {
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);
        this.cachedPermissionGroups.clear();
        this.cachedPermissionGroups.addAll(section.getAll());
    }


    @Override
    public void addPermissionGroup(PermissionGroup group) {
        if (this.getPermissionGroupByNameOrNull(group.getName()) != null) {
            return;
        }
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);
        this.cachedPermissionGroups.add(group);
        section.insert(group);
    }

    @Override
    public void deletePermissionGroup(String name) {
        PermissionGroup group = this.getPermissionGroupByNameOrNull(name);
        if (group == null) {
            return;
        }
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);
        this.cachedPermissionGroups.remove(group);
        section.delete(name);
    }

    @Nullable
    @Override
    public PermissionGroup getPermissionGroupByNameOrNull(@NotNull String name) {
        return this.cachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    @NotNull
    @Override
    public Task<PermissionGroup> getPermissionGroup(@NotNull String name) {
        return Task.callAsync(() -> this.cachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    @Override
    public void updatePermissionGroup(PermissionGroup group) {
        PermissionGroup oldGroup = this.getPermissionGroupByNameOrNull(group.getName());
        if (oldGroup == null) {
            return;
        }
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);

        int index = this.cachedPermissionGroups.indexOf(oldGroup);
        this.cachedPermissionGroups.set(index, group);

        section.update(group.getName(), (DefaultPermissionGroup) group);
    }

    @NotNull
    @Override
    public PermissionPlayer getPlayer(@NotNull CloudOfflinePlayer player) {
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);

        DefaultPermissionPlayer foundPlayer = section.findById(player.getMainIdentity());
        foundPlayer.setOfflinePlayer(player);

        return foundPlayer;
    }
}
