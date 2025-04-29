package cloud.hytora.modules.npc.spigot.task;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.CloudNPC;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCFunction;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import cloud.hytora.modules.npc.spigot.impl.SpigotNPCManager;
import lombok.AllArgsConstructor;
import org.bukkit.entity.Player;

@AllArgsConstructor
public class NPCManagerTask implements Runnable {

    private final EntityPlayerConnection connection;

    public void run() {
        Player player = connection.toPlayer();
        if (player == null) {
            return;
        }
        NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

        for (CloudNPC cloudnpc : factory.getActiveNPCs()) {
            SpigotNPC spigotNpc = (SpigotNPC) cloudnpc;

            boolean canSeeNPC = (player.getWorld().getName().equalsIgnoreCase(spigotNpc.getLocation().getWorld().getName()) && player.getLocation().distance(spigotNpc.getLocation()) <= SpigotNPCManager.VIEW_DISTANCE);

            if (spigotNpc.getViewers().stream().anyMatch(c -> c.getUniqueId().equals(connection.getUniqueId())) && !canSeeNPC) {
                spigotNpc.despawn(connection);
                continue;
            }
            if (canSeeNPC) {
                if (spigotNpc.getViewers().stream().noneMatch(c -> c.getUniqueId().equals(connection.getUniqueId()))) {
                    spigotNpc.spawn(connection);
                }
                if (spigotNpc.getMeta().isFunction(NPCFunction.LOOK)) {
                    spigotNpc.lookAt(connection, player.getLocation(), false);
                }
                spigotNpc.getHologram().updateNames(connection);
            }
        }
    }
}
