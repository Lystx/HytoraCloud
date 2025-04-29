package cloud.hytora.modules.npc.spigot.entity;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.modules.npc.spigot.entity.cache.CacheRegistry;
import cloud.hytora.modules.npc.spigot.entity.user.EntityPlayerConnection;
import cloud.hytora.modules.npc.spigot.entity.utility.Utils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Location;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@AllArgsConstructor
public class Hologram {


    private final List<HologramLine> hologramLines = new ArrayList<>();
    private final SpigotNPC spigotNpc;


    /**
     * Called when creating a {@link Hologram}.
     */
    public void createHologram() {
        spigotNpc.getViewers().forEach(this::delete);
        try {
            hologramLines.clear();
            double y = 0;
            final Location location = spigotNpc.getLocation();
            for (LineData line : spigotNpc.getMeta().getHologramLines()) {

                boolean visible = !line.getLine().get().equalsIgnoreCase("%space%"); // determine if the line should be seen
                Object armorStand = CacheRegistry.ENTITY_CONSTRUCTOR.load().newInstance(CacheRegistry.GET_HANDLE_WORLD_METHOD.load().invoke(location.getWorld()),
                        location.getX(), (location.getY() - 0.15) + (y), location.getZ());
                if (visible) {
                    CacheRegistry.SET_CUSTOM_NAME_VISIBLE_METHOD.load().invoke(armorStand, true); // entity name is not visible by default
                    updateLine(line.getLine().get(), armorStand, null);
                }
                CacheRegistry.SET_INVISIBLE_METHOD.load().invoke(armorStand, true);
                hologramLines.add(new HologramLine(line.getLine(),
                        armorStand, (Integer) CacheRegistry.GET_ENTITY_ID.load().invoke(armorStand)));
                y += 0.3;

            }
            setLocation(location, 0);
            spigotNpc.getPackets().flushCache("getHologramSpawnPacket");
            spigotNpc.getViewers().forEach(this::spawn);
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }

    /**
     * Spawns the hologram for the given player.
     *
     * @param user The player to spawn the hologram for.
     */
    public void spawn(EntityPlayerConnection user) {
        hologramLines.forEach(hologramLine -> {
            try {
                Object entityPlayerPacketSpawn = spigotNpc.getPackets().getProxyInstance()
                        .getHologramSpawnPacket(hologramLine.armorStand);
                Utils.sendPackets(user, entityPlayerPacketSpawn);
            } catch (ReflectiveOperationException operationException) {
                delete(user);
            }
        });
    }

    /**
     * Deletes the hologram for the given player.
     *
     * @param user The player to remove the hologram for.
     */
    public void delete(EntityPlayerConnection user) {
        hologramLines.forEach(hologramLine -> {
            try {
                Utils.sendPackets(user, spigotNpc.getPackets().getProxyInstance().getDestroyPacket(hologramLine.id));
            } catch (ReflectiveOperationException operationException) {
                throw new RuntimeException(operationException);
            }
        });
    }

    /**
     * Updates the hologram text for the given player.
     *
     * @param user The player to update the hologram for.
     */
    public void updateNames(EntityPlayerConnection user) {
        for (HologramLine hologramLine : hologramLines) {
            try {
                updateLine(hologramLine.line.get(), hologramLine.armorStand, user);
                // update the line
                Object metaData = spigotNpc.getPackets().getProxyInstance().getMetadataPacket(hologramLine.id, hologramLine.armorStand);
                Utils.sendPackets(
                        user,
                        metaData);
            } catch (ReflectiveOperationException operationException) {
                throw new RuntimeException(operationException);
            }
        }
    }

    /**
     * Updates the hologram location.
     */
    public void updateLocation() {
        hologramLines.forEach(hologramLine -> {
            try {
                Object packet = CacheRegistry.PACKET_PLAY_OUT_ENTITY_TELEPORT_CONSTRUCTOR.load().newInstance(hologramLine.armorStand);
                spigotNpc.getViewers().forEach(player -> Utils.sendPackets(player, packet));
            } catch (ReflectiveOperationException operationException) {
                throw new RuntimeException(operationException);
            }
        });
    }

    /**
     * Sets & updates the hologram location.
     *
     * @param location The new location.
     */
    public void setLocation(Location location, double height) {
        location = location.clone().add(0, height, 0);
        try {
            double y = spigotNpc.getMeta().getHologramHeight();
            for (HologramLine hologramLine : hologramLines) {
                CacheRegistry.SET_LOCATION_METHOD.load().invoke(hologramLine.armorStand,
                        location.getX(), (location.getY() - 0.15) + y,
                        location.getZ(), location.getYaw(), location.getPitch());
                y += 0.3;
            }
            updateLocation();
        } catch (ReflectiveOperationException operationException) {
            throw new RuntimeException(operationException);
        }
    }

    /**
     * Updates a hologram line.
     *
     * @param line       The new hologram line.
     * @param armorStand The hologram entity line.
     * @param user       The player to update the line for.
     * @throws InvocationTargetException If cannot invoke method.
     * @throws IllegalAccessException    If the method cannot be accessed.
     */
    private void updateLine(String line, Object armorStand, @Nullable EntityPlayerConnection user) throws InvocationTargetException, IllegalAccessException {
        if ((Utils.BUKKIT_VERSION > 12)) {
            CacheRegistry.SET_CUSTOM_NAME_NEW_METHOD.load().invoke(armorStand, CacheRegistry.CRAFT_CHAT_MESSAGE_METHOD.load().invoke(null, Utils.toColor(line)));
        } else {
            CacheRegistry.SET_CUSTOM_NAME_OLD_METHOD.load().invoke(armorStand, Utils.toColor(line));
        }
    }

    /**
     * Used to create new lines for a {@link Hologram}.
     */
    private static class HologramLine {
        /**
         * The hologram line string.
         */
        private final Supplier<String> line;
        /**
         * The hologram line entity.
         */
        private final Object armorStand;
        /**
         * The hologram line entity id.
         */
        private final int id;

        /**
         * Creates a new line for the hologram.
         *
         * @param line       The hologram line string.
         * @param armorStand The hologram entity.
         * @param id         The hologram entity id.
         */
        protected HologramLine(Supplier<String> line,
                               Object armorStand,
                               int id) {
            this.line = line;
            this.armorStand = armorStand;
            this.id = id;
        }
    }


    @AllArgsConstructor
    @Getter
    public static class LineData {


        private final Supplier<String> line;
        private final TimeUnit unit;
        private final long value;

        public LineData(String line) {
            this(() -> line, null, 0L);
        }

        public boolean isUpdate() {
            return unit != null && value != 0;
        }

    }
}
