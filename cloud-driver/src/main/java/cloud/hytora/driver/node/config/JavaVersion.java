package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface JavaVersion extends IBufferObject {

    int getId();

    String getName();

    String getPath();
}
