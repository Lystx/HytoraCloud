package cloud.hytora.driver.services.template.def;

import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.deployment.ServiceDeployment;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.template.TemplateManager;
import cloud.hytora.driver.services.template.TemplateStorage;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;

@Getter
public class DefaultTemplateManager implements TemplateManager {

    /**
     * All cached storages
     */
    private final Collection<TemplateStorage> storages = new ArrayList <>();

    @Override
    public void registerStorage(TemplateStorage storage) {
        if (this.getStorage(storage.getName()) == null) {
            this.storages.add(storage);
        }
    }

    @Override
    public void deployService(@NotNull ICloudServer server, @NotNull ServiceDeployment... deployments) {
        for (ServiceDeployment deployment : deployments) {
            ServiceTemplate template = deployment.getTemplate();
            TemplateStorage storage = template.getStorage();
            if (storage != null) {
                storage.deployService(server, deployment);
            }
        }
    }

    @Override
    public TemplateStorage getStorage(String name) {
        return this.storages.stream().filter(ts -> ts.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }
}
