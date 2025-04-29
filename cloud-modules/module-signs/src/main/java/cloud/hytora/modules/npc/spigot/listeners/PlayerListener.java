package cloud.hytora.modules.npc.spigot.listeners;

import cloud.hytora.common.scheduler.SchedulerFuture;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import cloud.hytora.modules.npc.spigot.task.NPCManagerTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {


    private final Map<UUID, Integer> TASK = new HashMap<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        EntityPlayerConnection connection = new EntityPlayerConnection(event.getPlayer().getUniqueId());
        connection.inject();

        EntityPlayerConnection.CONNECTIONS.put(event.getPlayer().getUniqueId(), connection);

        CloudDriver
                .getInstance()
                .getScheduler()
                .scheduleDelayedTask(() -> {
                    SchedulerFuture schedulerFuture = CloudDriver.getInstance().getScheduler().scheduleRepeatingTaskAsync(new NPCManagerTask(connection), 60L, 1L);

                    TASK.put(event.getPlayer().getUniqueId(), schedulerFuture.getId());
                }, 100L);

    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onQuit(PlayerQuitEvent event) {
        EntityPlayerConnection connection = EntityPlayerConnection.CONNECTIONS.get(event.getPlayer().getUniqueId());

        int taskId = TASK.get(event.getPlayer().getUniqueId());

        EntityPlayerConnection.CONNECTIONS.remove(event.getPlayer().getUniqueId());
        EntityPlayerConnection.ENTITY_TYPE.remove(event.getPlayer().getUniqueId());

        if (connection == null) {
            return;
        }

        CloudDriver.getInstance().getScheduler().cancelTask(taskId);


        NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

        factory.getActiveNPCs().stream()
                .filter(npc -> npc.getViewerIds()
                        .stream()
                        .anyMatch(uuid -> connection.getUniqueId().equals(uuid))
                )
                .forEach(npc -> {
                    npc.despawn(connection);
                });
        factory.updateCache();
    }

}
