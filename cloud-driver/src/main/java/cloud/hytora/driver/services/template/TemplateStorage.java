package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public interface TemplateStorage {

    String getName();

    void copyTemplate(@NotNull CloudServer server, @NotNull ServiceTemplate template, @NotNull File directory) throws Exception;

    void deleteTemplate(@NotNull ServiceTemplate template);

    void createTemplate(@NotNull ServiceTemplate template);

    void deployService(@NotNull CloudServer server, @NotNull ServiceDeployment deployment);

    /**
     * Closes this storage, after it has been closed, no more interaction to this storage should be done and might lead to
     * errors.
     *
     * @throws IOException if an I/O error occurred
     */
    void close() throws IOException;

}
