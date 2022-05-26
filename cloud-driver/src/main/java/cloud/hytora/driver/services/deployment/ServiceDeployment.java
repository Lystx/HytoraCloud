package cloud.hytora.driver.services.deployment;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.services.template.ServiceTemplate;

import java.util.Collection;

public interface ServiceDeployment extends Bufferable {

    ServiceTemplate getTemplate();

    Collection<String> getExclusionFiles();
}
