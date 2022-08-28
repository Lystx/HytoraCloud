package cloud.hytora.driver.services.template;

import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.deployment.IDeployment;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public interface ITemplateManager {

    Collection<ITemplateStorage> getStorages();

    void registerStorage(ITemplateStorage storage);

    void deployService(@NotNull ICloudServer server, @NotNull IDeployment... deployments);

    ITemplateStorage getStorage(String name);
}
