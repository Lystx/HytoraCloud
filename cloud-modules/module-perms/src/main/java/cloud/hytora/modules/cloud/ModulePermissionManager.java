package cloud.hytora.modules.cloud;

import cloud.hytora.common.function.ExceptionallySupplier;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.action.query.ExecutedQuery;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.player.CloudPlayerLoginFirstTimeEvent;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.modules.DefaultPermissionManager;
import cloud.hytora.modules.global.impl.DefaultPermission;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import cloud.hytora.modules.global.packets.PermsCacheUpdatePacket;
import cloud.hytora.modules.global.packets.PermsUpdatePlayerPacket;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

@Getter
public class ModulePermissionManager extends DefaultPermissionManager {

    private final List<PermissionGroup> cachedPermissionGroups;
    private final List<PermissionPlayer> allCachedPermissionPlayers;

    public ModulePermissionManager() {
        super();
        this.cachedPermissionGroups = new ArrayList<>();
        this.allCachedPermissionPlayers = new ArrayList<>();
        CloudDriver.getInstance().getEventManager().registerListener(this);
    }

    private void updateCache() {
        PermsCacheUpdatePacket packet = new PermsCacheUpdatePacket(getAllCachedPermissionGroups());

        packet.publish();
    }

    @NotNull
    @Override
    public Collection<PermissionGroup> getAllCachedPermissionGroups() {
        checkDouble();
        return cachedPermissionGroups;
    }

    public Task<Collection<PermissionGroup>> loadGroups() {
        Task<Collection<PermissionGroup>> collectionTask = Task.empty();
        Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();

        database.query("module_perms_groups")
                .executeAsync()
                .onTaskSucess(query -> {

                    if (query.isEmpty()) {
                        CloudDriver.getInstance().getLogger().info("Perms-Module loaded no PermissionGroups!", this.cachedPermissionGroups.size());
                        return;
                    }
                    this.cachedPermissionGroups.clear();
                    List<Document> collect = query.all().collect(Collectors.toList());
                    int i = 0;
                    for (Document document : collect) {
                        i++;
                        if (cachedPermissionGroups.stream().anyMatch(g -> g.getName().equalsIgnoreCase(document.getString("name")))) {
                            continue;
                        }
                        PermissionGroup group = new DefaultPermissionGroup();
                        group.setName(document.getString("name"));
                        group.setChatColor(document.getString("chatColor"));
                        group.setPrefix(document.getString("prefix"));
                        group.setSuffix(document.getString("suffix"));
                        group.setNamePrefix(document.getString("namePrefix"));
                        group.setSortId(document.getInt("sortId"));
                        group.setDefaultGroup(document.getBoolean("defaultGroup"));

                        for (String inheritedGroups : document.getString("inheritedGroups").split(",")) {
                            group.addInheritedGroup(inheritedGroups);
                        }

                        Document permDataDoc = Document.newJsonDocument(document.getString("permissionData"));
                        PermissionData permissionData = permDataDoc.toInstance(PermissionData.class);

                        group.setDeniedPermissions(permissionData.getDeniedPermissions());

                        for (String permission : permissionData.getPermissions().keySet()) {
                            long timeOut = permissionData.getPermissions().get(permission);
                            group.addPermission(new DefaultPermission(permission, timeOut));

                        }

                        Map<String, Collection<String>> taskPermissions = permissionData.getTaskPermissions();
                        Map<IServiceTask, Collection<String>> perms = new HashMap<>();

                        for (String key : taskPermissions.keySet()) {
                            Collection<String> value = taskPermissions.get(key);
                            IServiceTask task = CloudDriver.getInstance().getServiceTaskManager().getCachedServiceTask(key);
                            if (task == null) {
                                continue;
                            }
                            perms.put(task, value);
                        }
                        group.setTaskPermissions(perms);

                        this.cachedPermissionGroups.add(group);
                        if (i == collect.size()) {
                            collectionTask.setResult(cachedPermissionGroups);
                        }
                    }

                });


        //LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
        //LocalStorageSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);

        return collectionTask;
    }


    @Override
    public void addPermissionGroup(PermissionGroup group) {
        if (this.getPermissionGroupByNameOrNull(group.getName()) != null) {
            return;
        }
        //LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
        //LocalStorageSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);

        updatePermissionGroup(group);

        //this.cachedPermissionGroups.add(group);
        // section.insert(group);

        this.updateCache();
    }

    @Override
    public void deletePermissionGroup(String name) {
        PermissionGroup group = this.getPermissionGroupByNameOrNull(name);
        if (group == null) {
            return;
        }
        Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();


        //LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
        //LocalStorageSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);
        this.cachedPermissionGroups.remove(group);
        //section.delete(name);
        database.delete("module_perms_groups")
                .where("name", name)
                .executeAsync();
        this.updateCache();
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
        Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();

        //LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
        //LocalStorageSection<DefaultPermissionGroup> section = database.getSection(DefaultPermissionGroup.class);

        this.cachedPermissionGroups.removeIf(g -> g.getName().equalsIgnoreCase(group.getName()));
        this.cachedPermissionGroups.add(group);

        StringBuilder inheritedGroups = new StringBuilder();
        for (String inheritedGroup : group.getInheritedGroups()) {
            inheritedGroups.append(inheritedGroup).append(",");
        }

        Map<String, Long> permissions = new HashMap<>();

        for (Permission permission : group.getPermissions()) {
            permissions.put(permission.getPermission(), permission.getExpirationDate());
        }

        PermissionData permissionData = new PermissionData(
                permissions,
                group.getDeniedPermissions(),
                ((DefaultPermissionGroup) group).taskPerms()
        );

        database.insertOrUpdate("module_perms_groups")
                .where("name", group.getName())
                .set("name", group.getName())
                .set("chatColor", group.getChatColor())
                .set("prefix", group.getPrefix())
                .set("suffix", group.getSuffix())
                .set("namePrefix", group.getNamePrefix())
                .set("inheritedGroups", inheritedGroups.toString())
                .set("sortId", group.getSortId())
                .set("defaultGroup", group.isDefaultGroup())
                .set("permissionData", Document.newJsonDocument(permissionData).asRawJsonString())
                .executeAsync();

        //section.upsert(group.getName(), (DefaultPermissionGroup) group);
        this.updateCache();
    }

    @Override
    public void updatePermissionPlayer(PermissionPlayer player) {
        PermissionGroup oldGroup = allCachedPermissionPlayers.stream().filter(pp -> pp.getUniqueId().equals(player.getUniqueId())).findFirst().map(PermissionPlayer::getHighestGroup).orElse(null);

        this.allCachedPermissionPlayers.removeIf(p -> p.getUniqueId().equals(player.getUniqueId()));
        this.checkDouble();
        this.allCachedPermissionPlayers.add(player);


        Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();

        PlayerData data = new PlayerData(
                ((DefaultPermissionPlayer) player).permissions,
                ((DefaultPermissionPlayer) player).groups,
                ((DefaultPermissionPlayer) player).deniedPermissions,
                ((DefaultPermissionPlayer) player).taskPermissions
        );

        database.insertOrUpdate("module_perms_players")
                .where("uniqueId", player.getUniqueId())
                .set("name", player.getName())
                .set("uniqueId", player.getUniqueId())
                .set("data", Document.newJsonDocument(data).asRawJsonString())
                .executeAsync();


        if (player.isOnline()) {
            ICloudPlayer onlinePlayer = player.toOnlinePlayer();
            ICloudService server = onlinePlayer.getServer();
            ICloudService proxyServer = onlinePlayer.getProxyServer();

            if (server != null) {
                server.sendDocument(new PermsUpdatePlayerPacket(player));
            }
            if (proxyServer != null) {
                proxyServer.sendDocument(new PermsUpdatePlayerPacket(player));
            }
            onlinePlayer.editProperties(properties -> {
                properties.set("module_perms_highest_group", player.getHighestGroup().getName());
            });
        } else {

            CloudDriver.getInstance()
                    .getPlayerManager().
                    getOfflinePlayer(player.getUniqueId())
                    .onTaskSucess(offlinePlayer -> {
                        offlinePlayer.editProperties(properties -> {
                            properties.set("module_perms_highest_group", player.getHighestGroup().getName());
                        });
                    });
        }



        if (oldGroup == null) {
            return;
        }
        if (!oldGroup.getName().equalsIgnoreCase(player.getHighestGroup().getName())) { //if group has changed
            System.out.println("Player group changed from " + oldGroup.getName() + " to " + player.getHighestGroup().getName() + " for " + player.getName());
            ICloudPlayer cp = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(player.getUniqueId());
            if (cp == null || !cp.isOnline()) {
                return;
            }

            cp.executor().disconnect(

                    "§8§m------------------------------\n" +
                            "§8\n" +
                            "§cYour PermissionGroup has changed§8! §cPlease rejoin§c!\n" +
                            "§8\n" +
                            "§8§m------------------------------"
            );
        }
    }


    @Nullable
    @Override
    public PermissionPlayer getPlayerByNameOrNull(@NotNull String name) {
        return this.allCachedPermissionPlayers.stream().filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElseGet(
                (ExceptionallySupplier<PermissionPlayer>) () -> {

                    Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();

                    ExecutedQuery query = database
                            .query("module_perms_players")
                            .where("name", name)
                            .execute();

                    CloudDriver.getInstance().getLogger().debug("§cLoading PP!");

                    //LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
                    //LocalStorageSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);
                    //DefaultPermissionPlayer player = section.findByMatch("name", name);


                    DefaultPermissionPlayer player;

                    if (!query.isEmpty()) {

                        Document document = query.first().get();

                        player = new DefaultPermissionPlayer(document.getString("name"), document.getUniqueId("uniqueId"));

                        PlayerData playerData = Document.newJsonDocument(document.getString("data")).toInstance(PlayerData.class);

                        player.setPermissions(playerData.getPermissions());
                        player.setGroups(playerData.getGroups());
                        player.setDeniedPermissions(playerData.getDeniedPermissions());
                        player.setTP(playerData.getTaskPermissions());

                        CloudDriver.getInstance().getLogger().debug("§cLoaded {}", player.getName());
                    } else {
                        player = null;

                    }
                    return player;
                });
    }


    @Override
    public Task<PermissionPlayer> getPlayerAsyncByUniqueId(UUID uniqueId) {
        return Task.callAsync(() -> getPlayerByUniqueIdOrNull(uniqueId));
    }

    @Override
    public Task<PermissionPlayer> getPlayerAsyncByName(String name) {
        return Task.callAsync(() -> getPlayerByNameOrNull(name));
    }

    @Override
    public boolean hasEntry(UUID uniqueId) {
        if (allCachedPermissionPlayers.stream().anyMatch(p -> p.getUniqueId().equals(uniqueId))) {
            return true;
        }
        return CloudDriver
                .getInstance()
                
                .getProvider(IDatabaseManager.class)
                .getDatabase()
                .query("module_perms_players")
                .where("uniqueId", uniqueId)
                .executeUnsigned().isSet();
    }

    @javax.annotation.Nullable
    @Override
    public PermissionPlayer getPlayerByUniqueIdOrNull(@NotNull UUID uniqueId) {
        return this.allCachedPermissionPlayers.stream().filter(p -> p.getUniqueId().equals(uniqueId)).findFirst().orElseGet((ExceptionallySupplier<PermissionPlayer>) () -> {

            Database database = CloudDriver.getInstance().getProvider(IDatabaseManager.class).getDatabase();

            ExecutedQuery query = database
                    .query("module_perms_players")
                    .where("uniqueId", uniqueId)
                    .execute();



            /*LocalStorage database = CloudDriver.getInstance().getUnchecked(IDatabaseManager.class).getLocalStorage();
            LocalStorageSection<DefaultPermissionPlayer> section = database.getSection(DefaultPermissionPlayer.class);
            DefaultPermissionPlayer player = section.findById(uniqueId.toString());*/


            DefaultPermissionPlayer player = null;

            if (!query.isEmpty()) {

                Document document = query.first().get();

                player = new DefaultPermissionPlayer(document.getString("name"), document.getUniqueId("uniqueId"));

                PlayerData playerData = Document.newJsonDocument(document.getString("data")).toInstance(PlayerData.class);

                player.setPermissions(playerData.getPermissions());
                player.setGroups(playerData.getGroups());
                player.setDeniedPermissions(playerData.getDeniedPermissions());
                player.setTP(playerData.getTaskPermissions());

                CloudDriver.getInstance().getLogger().debug("§cLoaded {}", player.getName());
            }
            return player;
        });
    }


    @Override
    public void addToCache(PermissionPlayer player) {

        PermissionPlayer player1 = this.allCachedPermissionPlayers.stream().filter(p -> p.getUniqueId().equals(player.getUniqueId())).findFirst().orElse(null);
        if (player1 != null) {
            allCachedPermissionPlayers.remove(player1);
        }
        this.allCachedPermissionPlayers.add(player);
        this.checkDouble();

    }


    private void checkDouble() {
        Collection<PermissionPlayer> players = new ArrayList<>();
        for (PermissionPlayer allCachedPermissionPlayer : this.allCachedPermissionPlayers) {
            if (players.stream().anyMatch(p -> p.getUniqueId().equals(allCachedPermissionPlayer.getUniqueId()))) {
                continue;
            }
            players.add(allCachedPermissionPlayer);
        }

        this.allCachedPermissionPlayers.clear();
        this.allCachedPermissionPlayers.addAll(players);

    }

    @EventListener
    public void handleFirstJoin(CloudPlayerLoginFirstTimeEvent event) {
        ICloudPlayer player = event.getCloudPlayer();

        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            this.cachedPermissionGroups.stream().filter(PermissionGroup::isDefaultGroup).findFirst().ifPresent(firstGroup -> {
                player.editProperties(properties -> {
                    properties.set("module_perms_highest_group", firstGroup.getName());
                });
            });

        }, 50L);
    }

}
