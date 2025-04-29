package cloud.hytora.modules.npc.spigot.entity.user;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.api.CloudNPC;
import cloud.hytora.modules.npc.api.NPCFactory;
import cloud.hytora.modules.npc.api.NPCManager;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPC;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.npc.NPCAction;
import cloud.hytora.modules.npc.spigot.entity.SpigotNPCMeta;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;

final class UserEntityInteractHandler extends ChannelInboundHandlerAdapter {

    private static final long DEFAULT_NPC_INTERACT_INTERVAL_MS = 5_000;

    final Object lock = new Object();

    @GuardedBy("lock")
    final HashMap<NPCValuePair, Stopwatch> npcClickTimers = new HashMap<>();

    /**
     * A npc/value pair used in for tracking npc click cooldowns.
     */
    private static final class NPCValuePair implements Serializable {
        final SpigotNPC spigotNpc;
        final Object value;

        private NPCValuePair(SpigotNPC spigotNpc, Object value) {
            this.spigotNpc = spigotNpc;
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            NPCValuePair npcValuePair = (NPCValuePair) o;
            return spigotNpc == npcValuePair.spigotNpc && Objects.equals(value, npcValuePair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(spigotNpc, value);
        }
    }

    final EntityPlayerConnection user;

    public UserEntityInteractHandler(EntityPlayerConnection user) {
        this.user = Preconditions.checkNotNull(user);
    }

    /**
     * Attempts to start or retrieve a timer for the given NPC and associated value.
     */
    Stopwatch tryStartTiming(SpigotNPC spigotNpc, Object value) {
        NPCValuePair npcValuePair = new NPCValuePair(spigotNpc, value);
        synchronized (lock) {
            Stopwatch stopwatch = npcClickTimers.get(npcValuePair);
            if (stopwatch == null) {
                npcClickTimers.put(npcValuePair, stopwatch = Stopwatch.createStarted());
            }
            return stopwatch;
        }
    }

    /**
     * Resets the timer for the given NPC and value if the specified duration has elapsed.
     * If the elapsed time is greater than the provided duration, the timer is reset and
     * started again.
     *
     * <p>If the timer has not yet expired, the method returns {@code true} to indicate that the
     * action should not be reset. Otherwise, it returns {@code false}, signaling that the timer has
     * expired and was reset.
     */
    boolean resetTimerIfExpired(SpigotNPC spigotNpc, Object value, long millis) {
        Stopwatch stopwatch = tryStartTiming(spigotNpc, value);
        if (stopwatch.elapsed(TimeUnit.MILLISECONDS) > millis) {
            stopwatch.reset().start();
            return false;
        }
        return true;
    }

    /**
     * Handles incoming packets related to Entity interaction. When a packet of type {@link
     * CacheRegistry#PACKET_PLAY_IN_USE_ENTITY_CLASS} is received, it processes the entity ID
     * and checks if the NPC associated with that entity has any registered actions to perform.
     *
     * <p><b>Note:</b> If the packet is not of the expected type, the method simply passes the
     * message to the next handler in the pipeline for further processing.
     *
     * @param ctx the context for this handler.
     * @param msg the incoming message.
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (CacheRegistry.PACKET_PLAY_IN_USE_ENTITY_CLASS.isInstance(msg)) {
            int entityId = CacheRegistry.PACKET_IN_USE_ENTITY_ID_FIELD.load().getInt(msg);

            NPCFactory factory = CloudDriver.getInstance().getProvider(NPCManager.class).getNPCFactory(CloudDriver.getInstance().getServiceManager().thisService());

            for (CloudNPC cloudNPC : factory.getActiveNPCs()) {
                SpigotNPC spigotNpc = (SpigotNPC) cloudNPC;
                SpigotNPCMeta npcModel = spigotNpc.getMeta();
                if (spigotNpc.getEntityID() != entityId) {
                    continue;
                }
                /*if (resetTimerIfExpired(spigotNpc, spigotNpc.getEntityID(), DEFAULT_NPC_INTERACT_INTERVAL_MS)) {
                    return;
                }*/
                for (NPCAction npcAction : npcModel.getClickActions()) {
                    /*if (npcAction.getDelay() > 0) {
                        if (resetTimerIfExpired(spigotNpc, npcAction, npcAction.getDelay() * 1000L)) {
                            return;
                        }
                    }*/
                    npcAction.execute(user, npcAction.getAction());
                }
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}
