package cloud.hytora.driver.entity.services.template;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.deployment.ServiceDeployment;
import cloud.hytora.driver.networking.packets.other.PacketTemplate;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Collection;

public interface TemplateManager extends PacketHandler<PacketTemplate> {

    Collection<TemplateStorage> getStorages();

    void registerStorage(TemplateStorage storage);

    void deployService(@NotNull CloudService server, @NotNull ServiceDeployment... deployments);

    TemplateStorage getStorage(String name);

    Task<Collection<File>> getDirectoryContentsAsync(ServiceTemplate template, String storageName);
}
