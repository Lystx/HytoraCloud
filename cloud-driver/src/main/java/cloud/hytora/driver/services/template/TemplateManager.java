package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface TemplateManager {

    Collection<TemplateStorage> getStorages();

    void registerStorage(TemplateStorage storage);

    void deployService(@NotNull ICloudService server, @NotNull ServiceDeployment... deployments);

    TemplateStorage getStorage(String name);
}
