package cloud.hytora.modules.perms.cloud;

import cloud.hytora.common.function.ExceptionallySupplier;
import cloud.hytora.common.task.IPromise;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.DatabaseSection;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.perms.DefaultPermissionManager;
import cloud.hytora.modules.perms.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.perms.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.perms.global.packets.PermsCacheUpdatePacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Getter
public class ModulePermissionManager extends DefaultPermissionManager {

    private final List<PermissionGroup> cachedPermissionGroups;
    private final List<PermissionPlayer> allCachedPermissionPlayers;

    public ModulePermissionManager() {
        this.cachedPermissionGroups = new ArrayList<>();
        this.allCachedPermissionPlayers = new ArrayList<>();
    }

    private void update() {
        PermsCacheUpdatePacket packet = new PermsCacheUpdatePacket(this.cachedPermissionGroups, this.allCachedPermissionPlayers);
        packet.publishAsync();
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getAllCachedPermissionGroups() {
        return cachedPermissionGroups;
    }

    public void loadGroups() {
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);
        this.cachedPermissionGroups.clear();
        this.cachedPermissionGroups.addAll(section.getAll());

        this.update();
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

        this.update();
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

        this.update();
    }

    @Nullable
    @Override
    public PermissionGroup getPermissionGroupByNameOrNull(@NotNull String name) {
        return this.cachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }


    @NotNull
    @Override
    public IPromise<PermissionGroup> getPermissionGroup(@NotNull String name) {
        return IPromise.callAsync(() -> this.cachedPermissionGroups.stream().filter(g -> g.getName().equalsIgnoreCase(name)).findFirst().orElse(null));
    }

    @Override
    public void updatePermissionGroup(PermissionGroup group) {
        PermissionGroup oldGroup = this.getPermissionGroupByNameOrNull(group.getName());
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);

        if (oldGroup == null) {
            this.cachedPermissionGroups.add(group);
        } else {
            int index = this.cachedPermissionGroups.indexOf(oldGroup);
            this.cachedPermissionGroups.set(index, group);
        }
        section.upsert(group.getName(), (DefaultPermissionGroup) group);
        this.update();
    }

    @Override
    public void updatePermissionPlayer(PermissionPlayer player) {

        PermissionPlayer oldPlayer = this.getPlayerByUniqueIdOrNull(player.getUniqueId());
        if (oldPlayer == null) {
            this.allCachedPermissionPlayers.add(player);
        } else {
            int index = this.allCachedPermissionPlayers.indexOf(oldPlayer);
            this.allCachedPermissionPlayers.set(index, player);
        }
        if (player.getPermissionGroups().isEmpty()) {
            for (PermissionGroup group : this.getAllCachedPermissionGroups().stream().filter(PermissionGroup::isDefaultGroup).collect(Collectors.toList())) {
                player.addPermissionGroup(group);
            }
        }
        SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
        DatabaseSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);

        section.upsert(player.getUniqueId().toString(), (DefaultPermissionPlayer) player);
        this.update();
    }


    @Nullable
    @Override
    public PermissionPlayer getPlayerByNameOrNull(@NotNull String name) {
        return
                this.allCachedPermissionPlayers.
                        stream()
                        .filter(
                                p ->
                                        p
                                                .getName()
                                                .equalsIgnoreCase(name)
                        )
                        .findFirst()
                        .orElseGet(
                                (ExceptionallySupplier<PermissionPlayer>) () -> {
                                    SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
                                    DatabaseSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);
                                    DefaultPermissionPlayer player = section.findByMatch("name", name);
                                    if (player != null) {

                                        this.allCachedPermissionPlayers.add(player);
                                        this.update();
                                    }
                                    return player;
                                });
    }

    @Override
    public IPromise<PermissionPlayer> getPlayerAsyncByUniqueId(UUID uniqueId) {
        return IPromise.callAsync(() -> getPlayerByUniqueIdOrNull(uniqueId));
    }

    @Override
    public IPromise<PermissionPlayer> getPlayerAsyncByName(String name) {
        return IPromise.callAsync(() -> getPlayerByNameOrNull(name));
    }

    @javax.annotation.Nullable
    @Override
    public PermissionPlayer getPlayerByUniqueIdOrNull(@NotNull UUID uniqueId) {
        return this.allCachedPermissionPlayers.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElseGet((ExceptionallySupplier<PermissionPlayer>) () -> {
            SectionedDatabase database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();
            DatabaseSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);
            DefaultPermissionPlayer player = section.findById(uniqueId.toString());
            if (player != null) {

                this.allCachedPermissionPlayers.add(player);
                this.update();
            }
            return player;
        });
    }
}
