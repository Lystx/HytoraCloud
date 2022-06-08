package cloud.hytora.driver.services.task.bundle;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.services.task.TaskDownloadEntry;
import cloud.hytora.driver.services.task.ServiceTask;
import cloud.hytora.driver.services.template.ServiceTemplate;
import cloud.hytora.driver.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.services.utils.SpecificDriverEnvironment;

import java.util.Collection;

public interface TaskGroup extends Bufferable {

    String getName();
    String[] getJavaArguments();

    SpecificDriverEnvironment getEnvironment();
    ServiceShutdownBehaviour getShutdownBehaviour();

    Collection<TaskDownloadEntry> getDownloadEntries();
    Collection<ServiceTemplate> getTemplates();
    Collection<ServiceTask> getChildren();

}
