package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface TemplateManager {

    Collection<TemplateStorage> getStorages();

    void registerStorage(TemplateStorage storage);

    void deployService(@NotNull CloudServer server, @NotNull ServiceDeployment... deployments);

    TemplateStorage getStorage(String name);
}
