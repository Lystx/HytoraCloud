package cloud.hytora.modules.npc.spigot.entity.utility.location;

import cloud.hytora.common.location.impl.CloudEntityLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SpigotLocation extends CloudEntityLocation<Double, Float> {

    private Location bukkitLocation;

    public SpigotLocation(String worldName, double x, double y, double z, float yaw, float pitch) {
        super(x, y, z, yaw, pitch, worldName);
    }

    public SpigotLocation(Location location) {
        this(location.getWorld().getName(), location
                .getX(), location
                .getY(), location
                .getZ(), location
                .getYaw(), location
                .getPitch());
    }
    public SpigotLocation(CloudEntityLocation<Double, Float> location) {
        this(location.getWorld(), location
                .getX(), location
                .getY(), location
                .getZ(), location
                .getYaw(), location
                .getPitch());
    }


    public Location bukkitLocation() {
        if (this.bukkitLocation != null) {
            return this.bukkitLocation;
        }
        return this.bukkitLocation = new Location(
                Bukkit.getWorld(this.world),
                this.x,
                this.y,
                this.z,
                this.yaw,
                this.pitch
        );
    }
}
