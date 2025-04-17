package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.api.Database;
import cloud.hytora.driver.database.api.action.SQLColumn;
import cloud.hytora.driver.module.ModuleController;
import cloud.hytora.driver.module.controller.AbstractModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.networking.PacketProvider;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.cloud.ModulePermissionManager;
import cloud.hytora.modules.cloud.command.PermsCommand;
import cloud.hytora.modules.cloud.handler.GroupPacketHandler;
import cloud.hytora.modules.cloud.handler.PlayerPacketHandler;
import cloud.hytora.modules.cloud.handler.PlayerUpdatePacketHandler;
import cloud.hytora.modules.cloud.listener.SyncListener;
import cloud.hytora.modules.global.packets.*;

import java.util.concurrent.TimeUnit;

@ModuleConfiguration(
        name = "module-perms",
        main = PermsModule.class,
        author = "Lystx",
        description = "Implementation for integrated Permissions-System",
        version = "SNAPSHOT-1.0",
        website = "https://github.com/Lystx/HytoraCloud/tree/master/cloud-modules/module-perms",
        copyType = ModuleCopyType.ALL,
        environment = ModuleEnvironment.ALL
)
public class PermsModule extends AbstractModule {

    public PermsModule(ModuleController controller) {
        super(controller);
    }

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {

        Database db = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getDatabase();

        db.createTableSafely(
                "module_perms_groups",
                new SQLColumn("name", SQLColumn.Type.TEXT, 32),
                new SQLColumn("chatColor", SQLColumn.Type.TEXT, 32),
                new SQLColumn("prefix", SQLColumn.Type.TEXT, 32),
                new SQLColumn("suffix", SQLColumn.Type.TEXT, 32),
                new SQLColumn("namePrefix", SQLColumn.Type.TEXT, 32),
                new SQLColumn("inheritedGroups", SQLColumn.Type.TEXT, Integer.MAX_VALUE),
                new SQLColumn("sortId", SQLColumn.Type.INT, 10000),
                new SQLColumn("defaultGroup", SQLColumn.Type.BOOLEAN, 100),
                new SQLColumn("permissionData", SQLColumn.Type.TEXT, Integer.MAX_VALUE)

        );

        db.createTableSafely(
                "module_perms_players",
                new SQLColumn("name", SQLColumn.Type.TEXT, 32),
                new SQLColumn("uniqueId", SQLColumn.Type.TEXT, 36),
                new SQLColumn("data", SQLColumn.Type.TEXT, Integer.MAX_VALUE)

        );

        PacketProvider.autoRegister(PermsGroupPacket.class);
        PacketProvider.autoRegister(PermsPlayerRequestPacket.class);
        PacketProvider.autoRegister(PermsPlayerUpdatePacket.class);

        CloudDriver.getInstance()
                .getProviderRegistry()
                .setProvider(PermissionManager.class, new ModulePermissionManager());

        CloudDriver.getInstance()
                .getEventManager()
                .registerListener(new SyncListener());
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {




        //LocalStorage database = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class).getLocalStorage();
        //database.registerSection("module-perms-groups", DefaultPermissionGroup.class);
        //database.registerSection("module-perms-players", DefaultPermissionPlayer.class);

        PermissionManager pm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);
        ((ModulePermissionManager)pm).loadGroups().onTaskSucess(groups -> {

            if (groups.isEmpty()) {

                PermissionGroup permissionGroup = pm.createPermissionGroup("Player");
                permissionGroup.setDefaultGroup(true);
                permissionGroup.setChatColor("&7");
                permissionGroup.setPrefix("&7");
                permissionGroup.setSuffix("&7");
                permissionGroup.setSortId(9999);
                permissionGroup.setNamePrefix("&7");
                permissionGroup.addPermission(Permission.of("cloud.test.permanent.permission"));
                permissionGroup.addPermission(Permission.of("cloud.test.temporary.permission", TimeUnit.DAYS, 30));

                permissionGroup.update(); //make sure it's saved

                PermissionGroup nextGroup = pm.createPermissionGroup("Admin");
                nextGroup.setDefaultGroup(false);
                nextGroup.setChatColor("&4");
                nextGroup.setPrefix("&4");
                nextGroup.setSuffix("&7");
                nextGroup.setSortId(0);
                nextGroup.setNamePrefix("&4");
                nextGroup.addPermission(Permission.of("*"));
                nextGroup.addInheritedGroup("Player");

                nextGroup.update(); //make sure it's saved
                System.out.println("Added DefaultGroups");
            } else {
                CloudDriver.getInstance().getLogger().info("Perms-Module loaded {} PermissionGroups!", groups.size());

            }
        });


        //registering network handler
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new GroupPacketHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new PlayerPacketHandler());
        CloudDriver.getInstance().getExecutor().registerPacketHandler(new PlayerUpdatePacketHandler());

        //registering commands and parsers
        CloudDriver.getInstance().getCommandManager().registerCommand(new PermsCommand());
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionPlayer.class, PermissionPlayer::byName);
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionGroup.class, s -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(s));

    }
}
