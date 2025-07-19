package cloud.hytora.driver.entity.services.utils.version;

import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import lombok.*;

@AllArgsConstructor
@Getter
public enum VersionType {

    /**
     * The oldest ProxySoftware BungeeCord by SpigotMC
     */
    BUNGEE(SpecificDriverEnvironment.PROXY),

    /**
     * The oldest ProxySoftware BungeeCord by SpigotMC
     */
    VERA(SpecificDriverEnvironment.PROXY),

    /**
     * A newer, completely different ProxySoftware
     * Velocity by VelocityPowered
      */
    VELOCITY(SpecificDriverEnvironment.PROXY),

    /**
     * The most used Minecraft-Software Spigot
     */
    SPIGOT(SpecificDriverEnvironment.MINECRAFT),

    /**
     * An alternative Minecraft-Software Glowstone
     */
    GLOWSTONE(SpecificDriverEnvironment.MINECRAFT),

    /**
     * Should not be used
     */
    @Deprecated
    UNKNOWN(SpecificDriverEnvironment.UNKNOWN);


    /**
     * The environment this version type is for
     */
    private final SpecificDriverEnvironment environment;
}
