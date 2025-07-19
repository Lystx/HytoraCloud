package cloud.hytora.node.remote;

import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.template.TemplateStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class RemoteTemplateStorage implements TemplateStorage {

    @Override
    public String getName() {
        return "protocol";
    }

    @Override
    public void copyTemplate(@NotNull CloudService server, @NotNull ServiceTemplate template, @NotNull File directory) throws Exception {

    }

    @Override
    public void deleteTemplate(@NotNull ServiceTemplate template) {

    }

    @Override
    public void createTemplate(@NotNull ServiceTemplate template) {

    }

    @Override
    public void deployService(@NotNull CloudService server, @NotNull ServiceDeployment deployment) {

    }

    @Override
    public void close() throws IOException {

    }
}
