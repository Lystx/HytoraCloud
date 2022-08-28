package cloud.hytora.driver.services.deployment;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.template.ITemplate;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * A {@link IDeployment} describes the process of copying a {@link ICloudServer} into its {@link ITemplate}
 * With some more settings you can easily copy your temp-server into its template
 *
 * @author Lystx
 * @since SNAPSHOT-1.4
 */
public interface IDeployment extends IBufferObject {

    /**
     * The template that should be deployed
     *
     * @see ITemplate
     */
    @NotNull
    ITemplate getTemplate();

    /**
     * The files that should <b>ONLY</b> be included
     */
    @NotNull
    Collection<String> getOnlyIncludedFiles();

    /**
     * The files that should <b>NOT</b> be included
     */
    @NotNull
    Collection<String> getExclusionFiles();
}
