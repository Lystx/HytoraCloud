package cloud.hytora.driver.event.defaults.server;

import cloud.hytora.driver.event.CloudEvent;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor @Getter
@NoArgsConstructor
public class CloudServerCacheRegisterEvent implements ProtocolTansferableEvent {

    private ServiceInfo server;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                server = buf.readObject(SimpleServiceInfo.class);
                break;
            case WRITE:
                buf.writeObject(server);
                break;
        }
    }
}
