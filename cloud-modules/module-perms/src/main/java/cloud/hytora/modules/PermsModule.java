package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.DatabaseSection;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.permission.Permission;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.cloud.ModulePermissionManager;
import cloud.hytora.modules.cloud.command.PermsCommand;
import cloud.hytora.modules.impl.DefaultPermissionGroup;
import cloud.hytora.modules.impl.DefaultPermissionPlayer;

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
public class PermsModule extends DriverModule {

    @ModuleTask(id = 1, state = ModuleState.LOADED)
    public void load() {
        CloudDriver.getInstance()
                .getProviderRegistry()
                .setProvider(PermissionManager.class, new ModulePermissionManager())
                .as(ModulePermissionManager.class)
                .loadGroups();
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {
        IDatabaseManager dm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class);
        SectionedDatabase database = dm.getDatabase();
        if (database.getSection(DefaultPermissionGroup.class) == null) {

            database.registerSection("module-perms-groups", DefaultPermissionGroup.class);

            PermissionManager pm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class);
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
            nextGroup.addPermission(Permission.of("cloud.test.permanent.permission"));
            nextGroup.addPermission(Permission.of("cloud.test.temporary.permission", TimeUnit.DAYS, 30));

            nextGroup.update(); //make sure it's saved
        }
        database.registerSection("module-perms-players", DefaultPermissionPlayer.class);

        CloudDriver.getInstance().getCommandManager().registerCommand(new PermsCommand());
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionPlayer.class, PermissionPlayer::byName);
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionGroup.class, s -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(s));

    }
}
