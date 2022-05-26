package cloud.hytora.driver.services.template;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

public interface ServiceTemplate extends Bufferable {

    String getPrefix();

    String getName();

    String buildTemplatePath();

    String getStorageName();

    TemplateStorage getStorage();

    boolean shouldCopyToStatic();
}
