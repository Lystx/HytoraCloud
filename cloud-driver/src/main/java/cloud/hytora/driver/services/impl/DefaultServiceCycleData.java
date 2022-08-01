package cloud.hytora.driver.services.impl;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.IServiceCycleData;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@AllArgsConstructor
public class DefaultServiceCycleData implements IServiceCycleData {

    private Document data;
    private long timestamp;
    private int latency;


    public DefaultServiceCycleData(Document data) {
        this.data = data;
        this.latency = -1;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                data = buf.readDocument();
                timestamp = buf.readLong();
                latency = (int) (System.currentTimeMillis() - timestamp);
                break;

            case WRITE:
                buf.writeDocument(data);
                buf.writeLong(System.currentTimeMillis());
                break;
        }
    }
}
