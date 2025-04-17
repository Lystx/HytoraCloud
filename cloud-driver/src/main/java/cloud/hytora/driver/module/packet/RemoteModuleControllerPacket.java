package cloud.hytora.driver.module.packet;

import cloud.hytora.driver.module.controller.base.ModuleConfig;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor
public class RemoteModuleControllerPacket extends AbstractPacket {

    public RemoteModuleControllerPacket(ModuleConfig config, RemoteModuleControllerPacket.PayLoad payload) {
        super(buf -> buf.writeEnum(payload).writeObject(config));
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

    }


    public enum PayLoad {

        LOAD_MODULE,

        ENABLE_MODULE,

        DISABLE_MODULE,

        RELOAD_MODULE,

        UNREGISTER_MODULE,

        RELOAD_CONFIG,

        GET_JAR_FILE,

        GET_DATA_FOLDER,
    }
}
