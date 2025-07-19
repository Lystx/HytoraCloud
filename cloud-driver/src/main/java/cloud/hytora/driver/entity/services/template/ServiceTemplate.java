package cloud.hytora.driver.entity.services.template;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import java.io.File;
import java.util.Collection;

public interface ServiceTemplate extends IBufferObject {

    String getPrefix();

    String getName();

    String buildTemplatePath();

    Task<Collection<File>> getDirectoryContentsAsync(String dirName);

    default File buildTemplateDirectory() {
        return new File(CloudDriver.Constants.TEMPLATES_DIR, buildTemplatePath());
    }

    String getStorageName();

    TemplateStorage getStorage();

    boolean shouldCopyToStatic();
}
