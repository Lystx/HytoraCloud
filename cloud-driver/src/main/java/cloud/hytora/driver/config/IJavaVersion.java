package cloud.hytora.driver.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface IJavaVersion extends IBufferObject {

    int getId();

    String getName();

    String getPath();
}
