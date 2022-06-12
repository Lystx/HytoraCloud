package cloud.hytora.driver.networking.packets;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.driver.DriverStorageUpdateEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class StorageUpdatePacket extends Packet {

    private StoragePayLoad payLoad;
    private cloud.hytora.document.Document storage;


    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                payLoad = buf.readEnum(StoragePayLoad.class);
                storage = buf.readDocument();

                CloudDriver.getInstance().getEventManager().callEventGlobally(new DriverStorageUpdateEvent());
                break;

            case WRITE:
                buf.writeEnum(payLoad);
                buf.writeDocument(storage);
                break;
        }
    }


    public enum StoragePayLoad {

        FETCH,

        UPDATE

    }
}
