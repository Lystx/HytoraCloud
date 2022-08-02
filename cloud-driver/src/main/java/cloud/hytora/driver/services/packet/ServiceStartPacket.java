package cloud.hytora.driver.services.packet;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.node.base.AbstractNode;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.impl.DriverServiceObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class ServiceStartPacket extends AbstractPacket {

    private ICloudServer cloudServer;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeObject(cloudServer);
                break;
            case READ:
                cloudServer = buf.readObject(DriverServiceObject.class);
                break;
        }
    }
}
