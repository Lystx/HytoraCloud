package cloud.hytora.driver.services;

import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.version.ServiceVersion;

import java.util.UUID;

public interface ConfigurableService {

    ConfigurableService port(int port);

    ConfigurableService memory(int memoryInMB);

    ConfigurableService motd(String motd);

    ConfigurableService uniqueId(UUID uniqueId);

    ConfigurableService properties(Document document);

    ConfigurableService maxPlayers(int maxPlayers);

    ConfigurableService node(String node);

    ConfigurableService templates(ServiceTemplate... templates);

    ConfigurableService version(ServiceVersion version);


    ConfigurableService ignoreIfLimitOfServicesReached();


    Task<ICloudServer> start();
}
