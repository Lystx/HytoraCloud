package cloud.hytora.driver.networking.packets.node;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.driver.services.impl.SimpleServiceInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NodeRequestServerStartPacket extends AbstractPacket {

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
