package cloud.hytora.driver.entity.services.utils;

/**
 * The {@link SpecificDriverEnvironment} defines the specific environment of
 * each {@link cloud.hytora.driver.entity.services.task.bundle.TaskGroup} and all its sub {@link cloud.hytora.driver.entity.services.CloudService}
 *
 *
 * @author Lystx
 * @version DEV-0.1
 * @since DEV-0.1
 *
 * @see cloud.hytora.driver.CloudDriver.Environment
 */
public enum SpecificDriverEnvironment {

    /**
     * This should not be used
     */
    @Deprecated
    UNKNOWN,

    /**
     * All services are proxy instances
     * (different versions possible: bungeeCord, velocity etc)
     * but the core is the same => proxy
     */
    PROXY,

    /**
     * All services are minecraft instances
     * (different versions possible: spigot, paper, sponge etc)
     * but the core is the same => minecraft
     */
    MINECRAFT

}
