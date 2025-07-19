package cloud.hytora.driver.event;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface NetworkEvent extends LocalEvent, IBufferObject {
}
