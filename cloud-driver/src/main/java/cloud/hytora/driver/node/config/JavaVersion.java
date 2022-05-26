package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;

public interface JavaVersion extends Bufferable {

    int getId();

    String getName();

    String getPath();
}
