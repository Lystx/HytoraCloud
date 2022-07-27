package cloud.hytora.modules;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.database.IDatabaseManager;
import cloud.hytora.driver.database.SectionedDatabase;
import cloud.hytora.driver.module.controller.DriverModule;
import cloud.hytora.driver.module.controller.base.ModuleConfiguration;
import cloud.hytora.driver.module.controller.base.ModuleCopyType;
import cloud.hytora.driver.module.controller.base.ModuleEnvironment;
import cloud.hytora.driver.module.controller.base.ModuleState;
import cloud.hytora.driver.module.controller.task.ModuleTask;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.impl.DefaultCloudOfflinePlayer;
import cloud.hytora.modules.cloud.ModulePermissionManager;
import cloud.hytora.modules.cloud.command.PermsCommand;
import cloud.hytora.modules.impl.DefaultPermissionGroup;
import cloud.hytora.modules.impl.DefaultPermissionPlayer;

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
        CloudDriver.getInstance().getProviderRegistry().upsert(PermissionManager.class, new ModulePermissionManager());

        IDatabaseManager dm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(IDatabaseManager.class);
        SectionedDatabase database = dm.getDatabase();
        database.registerSection("module-perms-players", DefaultPermissionPlayer.class);
        database.registerSection("module-perms-groups", DefaultPermissionGroup.class);
    }

    @ModuleTask(id = 2, state = ModuleState.ENABLED)
    public void enable() {
        CloudDriver.getInstance().getCommandManager().registerCommand(new PermsCommand());
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionPlayer.class, PermissionPlayer::ofOfflineByName);
        CloudDriver.getInstance().getCommandManager().registerParser(PermissionGroup.class, s -> CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPermissionGroupByNameOrNull(s));

        //CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).
    }
}
