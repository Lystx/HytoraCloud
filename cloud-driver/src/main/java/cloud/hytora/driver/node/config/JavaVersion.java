package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

public interface JavaVersion extends Bufferable {

    String getName();

    String getPath();
}
