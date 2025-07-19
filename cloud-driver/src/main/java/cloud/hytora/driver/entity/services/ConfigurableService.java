package cloud.hytora.driver.entity.services;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.utils.version.ServiceVersion;

import java.util.UUID;

/**
 * A {@link ConfigurableService} is basically the same as a CloudService-Builder if you wanna call it like that.
 * Here you can pre-define all the variables for the start of a {@link CloudService}.
 * If you don't provide any arguments it will simply take the most important ones from the parent {@link ServiceTask}
 *
 * @since DEV-1.4
 * @version STABLE-2.0
 * @see ServiceTask#configureFutureService()
 */
public interface ConfigurableService {

    /**
     * Sets a custom port for this service
     * If no custom port is set, the cloud will
     * automatically choose the next free port for you
     *
     * @param port the custom port
     * @return current configurator instance
     */
    ConfigurableService port(int port);

    /**
     * Sets the seconds the new service will stay
     * online if no players joined.
     * If players join within the given time, the timer will stop.
     * Should all players leave, the timer will resume.
     * This is to save unused resources.
     *
     * @param seconds the timer seconds
     * @return current configurator instance
     */
    ConfigurableService timeOutIfNoPlayers(int seconds);

    /**
     * Sets a custom memory for this service
     * If no custom memory is set, it will
     * take the value from the {@link ServiceTask}
     *
     * @param memoryInMB the custom memory in megaBytes
     * @return current configurator instance
     */
    ConfigurableService memory(int memoryInMB);

    /**
     * Sets a custom motd for this service
     * If no custom memory is set, it will
     * take the value from the {@link ServiceTask}
     *
     * @param motd the custom motd
     * @return current configurator instance
     */
    ConfigurableService motd(String motd);

    /**
     * Sets a custom uniqueId for this service
     * If no custom uniqueId is set, it will
     * generate a random UUID
     *
     * @param uniqueId the custom uniqueId
     * @return current configurator instance
     */
    ConfigurableService uniqueId(UUID uniqueId);

    /**
     * Sets custom properties for this service
     * If no custom uniqueId is set, it will
     * create empty properties
     *
     * @param document the custom properties
     * @return current configurator instance
     * @see Document
     */
    ConfigurableService properties(Document document);

    /**
     * Sets a custom amount of maxPlayers for this service
     * If no custom uniqueId is set, it will
     * take the value from the {@link ServiceTask}
     *
     * @param maxPlayers the custom maxPlayers
     * @return current configurator instance
     */
    ConfigurableService maxPlayers(int maxPlayers);

    /**
     * Sets a custom node for this service
     * If no custom node is set, it will
     * take the value from the {@link ServiceTask}
     *
     * @param node the custom node
     * @return current configurator instance
     */
    ConfigurableService node(String node);

    /**
     * Sets a custom template for this service
     * If no custom node is set, it will
     * use the "default" template
     *
     * @param templates the custom template(s)
     * @return current configurator instance
     */
    ConfigurableService templates(ServiceTemplate... templates);

    /**
     * Sets a custom version for this service
     * If no custom version is set, it will
     * take the value from the {@link ServiceTask}
     *
     * @param version the custom version
     * @return current configurator instance
     */
    ConfigurableService version(ServiceVersion version);

    /**
     * Sets a custom world for this service to load
     * It will then rename the directory to "world"
     * to be loaded on start of the service
     *
     * @param template the template to find the world in
     * @param filePath the path of the world folder inside the template
     * @return current configurator instance
     */
    ConfigurableService defaultWorld(ServiceTemplate template, String filePath);

    /**
     * If this option is activated the starter will
     * ignore if the amount of {@link ServiceTask#getMaxOnlineService()} is reached
     *
     * @return current configurator instance
     */
    ConfigurableService ignoreIfLimitOfServicesReached();

    /**
     * This method will start the configured service
     * The task will complete when the {@link CloudService} is marked as ready
     *
     * @return task instance
     * @see Task
     * @see CloudService#isReady()
     */
    Task<CloudService> start();

}
