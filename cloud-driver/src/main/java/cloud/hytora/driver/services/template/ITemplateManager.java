package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ITemplateManager {

    Collection<TemplateStorage> getStorages();

    void registerStorage(TemplateStorage storage);

    void deployService(@NotNull ICloudServer server, @NotNull ServiceDeployment... deployments);

    TemplateStorage getStorage(String name);
}
