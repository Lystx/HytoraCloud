package cloud.hytora.driver.entity.services.task.bundle;

import cloud.hytora.common.identification.ImmutableNameHolder;
import cloud.hytora.driver.common.objects.PlaceHolder;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;
import cloud.hytora.driver.entity.services.utils.ServiceShutdownBehaviour;
import cloud.hytora.driver.entity.services.utils.SpecificDriverEnvironment;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.entity.services.task.TaskDownloadEntry;
import cloud.hytora.driver.entity.services.task.ServiceTask;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A {@link TaskGroup} is the parent of unlimited {@link ServiceTask}s
 * For example you have multiple modes of a Gamemode that run under the same name and you want
 * to categorize it, you use a {@link TaskGroup}.
 * Example:<br>
 * <br>
 *  ServiceTask: BedWars-8x1    <br>
 *  ServiceTask: BedWars-4x4<br>
 *  ServiceTask: BedWars-4x3           => TaskGroup: BedWars<br>
 *  ServiceTask: BedWars-4x2<br>
 *  ServiceTask: BedWars-8x4<br>
 * <br>
 *
 *
 * So you can define the same javaArguments {@link #getJavaArguments()} the same shutdownBehaviour
 * and so on for all underlying {@link ServiceTask}s
  *
 * @author Lystx
 * @since STABLE-1.0
 * @version STABLE-1.6
 *
 * @see ServiceTask
 * @see ServiceTemplate
 */
public interface TaskGroup extends IBufferObject, ImmutableNameHolder, PlaceHolder {

    /**
     * The custom java arguments when
     * starting a {@link CloudService} of this task
     */
    String[] getJavaArguments();

    /**
     * The environment of services that will be under this group
     *
     * @see SpecificDriverEnvironment
     */
    @NotNull
    SpecificDriverEnvironment getEnvironment();

    /**
     * The behaviour when a service of this group
     * will be shut down
     *
     * @see ServiceShutdownBehaviour
     */
    @NotNull
    ServiceShutdownBehaviour getShutdownBehaviour();

    /**
     * The {@link TaskDownloadEntry}s for this group
     * that will be downloaded from the internet on every start of a server
     */
    @NotNull
    Collection<TaskDownloadEntry> getDownloadEntries();

    /**
     * Collects every {@link ServiceTemplate} for this group
     *
     * @see ServiceTemplate
     */
    @NotNull
    Collection<ServiceTemplate> getTemplates();


    /**
     * Collects every {@link ServiceTask} for this group
     *
     * @see ServiceTask
     */
    @NotNull
    Collection<ServiceTask> getChildren();

}
