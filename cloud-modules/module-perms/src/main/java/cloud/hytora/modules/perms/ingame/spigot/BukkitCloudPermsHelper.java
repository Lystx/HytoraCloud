package cloud.hytora.modules.perms.ingame.spigot;

import cloud.hytora.common.misc.ReflectionUtils;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;

public class BukkitCloudPermsHelper {

    private BukkitCloudPermsHelper() {}


    public static void injectPermissible(Player player) {
        try {
            Field field = ReflectionUtils.getInheritedPrivateField(player.getClass(), "perm");
            field.setAccessible(true);
            field.set(player, new BukkitCloudPermissible(player));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        if (player.hasPermission("*")) {
            player.setOp(true);
        }
    }
}
