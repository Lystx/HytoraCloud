package de.lystx.hytoracloud.driver.service.fallback;

import de.lystx.hytoracloud.driver.player.ICloudPlayer;
import de.lystx.hytoracloud.driver.service.IService;

import java.util.List;

public interface IFallbackManager {

    /**
     * Checks if a {@link ICloudPlayer} is already on a Fallback-server
     *
     * @param player the player
     * @return boolean if on fallback
     */
    boolean isFallback(ICloudPlayer player);

    /**
     * Searches for a free {@link Fallback} as {@link IService}
     * If no service is found it will return null
     *
     * @param player the player
     * @return fallback as service
     */
    IService getFallback(ICloudPlayer player);

    /**
     * Searches for a free {@link Fallback} as {@link IService}
     * it searches every online service except the provided
     *
     * @param player the player
     * @param service the service that should be ignored
     * @return fallback as service
     */
    IService getFallbackExcept(ICloudPlayer player, IService service);

    /**
     * Gets the {@link Fallback} with the highest priority
     * for this player and only if he is allowed to join it
     *
     * @param player the player
     * @return fallback or null
     */
    Fallback getHighestFallback(ICloudPlayer player);

    /**
     * Gets a list of all available {@link Fallback}s for
     * a given {@link ICloudPlayer}
     *
     * @param player the player
     * @return fallbacks
     */
    List<Fallback> getFallbacks(ICloudPlayer player);
}
