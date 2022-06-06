package cloud.hytora.driver.services.configuration.bundle;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.services.configuration.ConfigurationDownloadEntry;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;

import java.util.Collection;

public interface ConfigurationParent extends Bufferable {

    String getName();
    String[] getJavaArguments();

    SpecificDriverEnvironment getEnvironment();
    ServiceShutdownBehaviour getShutdownBehaviour();

    Collection<ConfigurationDownloadEntry> getDownloadEntries();
    Collection<ServiceTemplate> getTemplates();
    Collection<ServerConfiguration> getChildren();

}
