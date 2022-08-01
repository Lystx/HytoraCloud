package cloud.hytora.driver.services;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface IServiceCycleData extends IBufferObject {

    Document getData();

    int getLatency();

    long getTimestamp();
}
