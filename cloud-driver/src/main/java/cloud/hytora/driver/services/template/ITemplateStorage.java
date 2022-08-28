package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.deployment.IDeployment;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * The {@link ITemplateStorage} manages all kinds of {@link ITemplate}s and performs different template-actions
 * for starting {@link ICloudServer}s but also deploys {@link ICloudServer}s using their {@link IDeployment}
 *
 * @author Lystx
 * @since SNAPSHOT-1.2
 */
public interface ITemplateStorage {

    /**
     * Returns the name of this storage (e.g. "local")
     */
    @NotNull
    String getName();

    /**
     * Copies an {@link ITemplate} into the provided directory for a newly generated {@link ICloudServer}
     *
     * @param server the server to copy for
     * @param template the template to copy
     * @param directory the directory to copy to
     * @throws Exception if something went wrong while copying
     */
    void copyTemplate(@NotNull ICloudServer server, @NotNull ITemplate template, @NotNull File directory) throws Exception;

    /**
     * Deletes an existing {@link ITemplate} from this storage's cache
     *
     * @param template the template to delete
     */
    void deleteTemplate(@NotNull ITemplate template);

    /**
     * Creates a new {@link ITemplate} and also sets up the
     * default-folders that this template requires
     *
     * @param template the template to create
     */
    void createTemplate(@NotNull ITemplate template);

    /**
     * Deploys the {@link ICloudServer} with the given {@link IDeployment}
     *
     * @param server the server to deploy
     * @param deployment the deployment
     * @see IDeployment
     */
    void deployService(@NotNull ICloudServer server, @NotNull IDeployment deployment);

    /**
     * Closes this storage, after it has been closed, no more interaction to this storage should be done and might lead to
     * errors.
     *
     * @throws IOException if an I/O error occurred
     */
    void close() throws IOException;

}
