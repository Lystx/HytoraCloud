package cloud.hytora.driver.networking.packets.group;


import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.services.configuration.ServerConfiguration;
import cloud.hytora.driver.services.configuration.SimpleServerConfiguration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ServiceConfigurationExecutePacket extends Packet {

    private ServerConfiguration configuration;
    private ExecutionPayLoad payLoad;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                this.configuration = buf.readObject(SimpleServerConfiguration.class);
                this.payLoad = buf.readEnum(ExecutionPayLoad.class);
                break;

            case WRITE:
                buf.writeObject(configuration);
                buf.writeEnum(payLoad);
                break;
        }
    }

    public enum ExecutionPayLoad {
        REMOVE, CREATE
    }

}
