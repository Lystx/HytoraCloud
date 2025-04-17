package cloud.hytora.driver.node.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.impl.UniversalCloudServer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class NodeRequestServerStartPacket extends AbstractPacket {

    private ICloudService server;

    private boolean demandsResponse;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                server = buf.readObject(UniversalCloudServer.class);
                demandsResponse = buf.readBoolean();
                break;

            case WRITE:
                buf.writeObject(server);
                buf.writeBoolean(demandsResponse);
                break;
        }
    }
}
