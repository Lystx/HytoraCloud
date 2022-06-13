package cloud.hytora.driver.services.utils.version;

import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;
import lombok.*;

@AllArgsConstructor
@Getter
public enum VersionType {

    /**
     * The oldest ProxySoftware BungeeCord by SpigotMC
     */
    BUNGEE(SpecificDriverEnvironment.PROXY),

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
    GLOWSTONE(SpecificDriverEnvironment.MINECRAFT);


    /**
     * The environment this version type is for
     */
    private final SpecificDriverEnvironment environment;
}
