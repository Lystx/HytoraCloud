package cloud.hytora.driver.networking.packets.services;

import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.Packet;
import cloud.hytora.driver.services.CloudServer;
import cloud.hytora.driver.services.impl.SimpleCloudServer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
@Getter
@AllArgsConstructor
public class CloudServerCacheUpdatePacket extends Packet {

    private CloudServer service;

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {

            case READ:
                service = buf.readObject(SimpleCloudServer.class);
                break;

            case WRITE:
                buf.writeObject(service);
                break;
        }
    }
}
