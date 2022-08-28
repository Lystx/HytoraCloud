package cloud.hytora.driver.services.template;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.services.task.IServiceTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Describes the template that different {@link IServiceTask}s have to define
 * which data is being copied to certain servers
 *
 * @author Lystx
 * @since SNAPSHOT-1.2
 */
public interface ITemplate extends IBufferObject {

    /**
     * The prefix (e.g. "default")
     */
    @NotNull
    String getPrefix();

    /**
     * The name of this template (e.g. "Lobby")
     */
    @NotNull
    String getName();

    /**
     * The storage name (e.g. "local")
     */
    @NotNull
    String getStorageName();

    /**
     * Returns the {@link ITemplateStorage} that is registered
     * under the name stored in {@link #getStorageName()}
     * If no such storage exists, this method will return null
     *
     * @return storage instance or null
     */
    @Nullable
    ITemplateStorage getStorage();

    /**
     * If this template should be copied into static services
     * every time they boot up
     */
    boolean shouldCopyToStatic();

    /**
     * Builds the template path
     */
    @NotNull
    String buildTemplatePath();

}
