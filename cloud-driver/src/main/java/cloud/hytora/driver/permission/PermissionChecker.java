package cloud.hytora.driver.permission;

import cloud.hytora.driver.command.sender.CommandSender;

import java.util.UUID;

public interface PermissionChecker {


    boolean hasPermission(UUID playerUniqueId, String permission);
}
