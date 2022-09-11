package cloud.hytora.driver.services;

import cloud.hytora.common.task.IPromise;
import cloud.hytora.document.Document;
import cloud.hytora.driver.services.task.IServiceTask;
import cloud.hytora.driver.services.template.ITemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;

import java.util.UUID;

/**
 * This interface represents an empty {@link ICloudServer} that is now being filled with values
 * You can here set port, memory, motd, UUID, etc. and then start it using {@link #start()}
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface IFutureCloudServer {

    /**
     * Sets the port of this server
     * <b>ATTENTION:</b> Keep in mind to only use free ports
     * <b>NOTE:</b> If you're unsure just don't use this method and
     * the program will auto-generate a new port
     *
     * @param port the port to set
     * @return current builder instance
     */
    IFutureCloudServer port(int port);

    /**
     * Sets the memory of this server
     * <b>ATTENTION:</b> Keep in mind to only use as much memory as your node can hold
     *
     * @param memoryInMB the memoryInMB to set
     * @return current builder instance
     */
    IFutureCloudServer memory(int memoryInMB);

    /**
     * Sets the motd of this server
     *
     * @param motd the motd to set
     * @return current builder instance
     */
    IFutureCloudServer motd(String motd);

    /**
     * Sets the uuid of this server
     *
     * @param uniqueId the uuid to set
     * @return current builder instance
     */
    IFutureCloudServer uniqueId(UUID uniqueId);

    /**
     * Sets the properties of this server
     *
     * @param document the properties to set
     * @return current builder instance
     */
    IFutureCloudServer properties(Document document);

    /**
     * Sets the maxPlayers of this server
     *
     * @param maxPlayers the maxPlayers to set
     * @return current builder instance
     */
    IFutureCloudServer maxPlayers(int maxPlayers);

    /**
     * Sets the node that the server will run on
     *
     * @param node the node to set
     * @return current builder instance
     */
    IFutureCloudServer node(String node);

    /**
     * Sets the templates that the server will use
     *
     * @param templates the templates to set
     * @return current builder instance
     */
    IFutureCloudServer templates(ITemplate... templates);

    /**
     * Sets the version that the server will use
     *
     * @param version the version to set
     * @return current builder instance
     */
    IFutureCloudServer version(ServiceVersion version);

    /**
     * Enables option that the limit will be ignored if
     * the {@link IServiceTask#getMaxOnlineService()} is reached
     *
     * @return current builder instance
     */
    IFutureCloudServer ignoreIfLimitOfServicesReached();

    /**
     * Returns an {@link IPromise} that will complete when the
     * server is registered as "ready"
     *
     * @return task with started {@link ICloudServer}
     */
    IPromise<ICloudServer> start();
}
