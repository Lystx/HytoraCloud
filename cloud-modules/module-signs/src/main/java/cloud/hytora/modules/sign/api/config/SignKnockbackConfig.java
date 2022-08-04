package cloud.hytora.modules.sign.api.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignKnockbackConfig {

    /**
     * If the config is enabled
     */
    private final boolean enabled;

    /**
     * The push strength
     */
    private final double strength;

    /**
     * The push distance
     */
    private final double distance;

    /**
     * The permission to bypass knockback
     */
    private final String byPassPermission;
}
