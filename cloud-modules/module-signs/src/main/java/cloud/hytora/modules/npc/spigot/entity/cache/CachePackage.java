package cloud.hytora.modules.npc.spigot.entity.cache;

import cloud.hytora.modules.npc.spigot.entity.utility.Utils;

public enum CachePackage {
    DEFAULT,
    CRAFT_BUKKIT("org.bukkit.craftbukkit." + Utils.getBukkitPackage()),
    MINECRAFT_SERVER("net.minecraft");

    private final String fixedPackageName;

    CachePackage(String packageName) {
        this

                .fixedPackageName = Utils.versionNewer(17) ? packageName : (packageName + (packageName.contains("minecraft") ? (".server." + Utils.getBukkitPackage()) : ""));
    }

    CachePackage() {
        this.fixedPackageName = "";
    }

    public String getForCategory(CacheCategory packetCategory, String extra) {
        return Utils.versionNewer(17) ? (
                packetCategory.getPackageName() + ((extra.length() > 0) ? ("." + extra) : "")) :
                this.fixedPackageName;
    }

    public String getFixedPackageName() {
        return this.fixedPackageName;
    }
}
