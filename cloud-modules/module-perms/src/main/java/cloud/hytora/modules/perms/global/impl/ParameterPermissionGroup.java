package cloud.hytora.modules.perms.global.impl;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.parameter.CommandParameterType;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;

public class ParameterPermissionGroup extends CommandParameterType<PermissionPlayer> {
    @Override
    public String label() {
        return "permissionPlayer";
    }

    @Override
    public PermissionPlayer resolve(String s) {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(PermissionManager.class).getPlayerByNameOrNull(s);
    }

    @Override
    public Class<PermissionPlayer> typeClass() {
        return PermissionPlayer.class;
    }

    @Override
    public boolean checkCustom(String arg, String s) {
        return false;
    }

    @Override
    public String handleCustomException(String s) {
        return null;
    }
}
