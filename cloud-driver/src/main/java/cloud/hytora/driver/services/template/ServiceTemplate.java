package cloud.hytora.driver.services.template;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface ServiceTemplate extends IBufferObject {

    String getPrefix();

    String getName();

    String buildTemplatePath();

    String getStorageName();

    TemplateStorage getStorage();

    boolean shouldCopyToStatic();
}
