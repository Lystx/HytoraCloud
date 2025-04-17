package cloud.hytora.modules.global.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.message.DocumentPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PermsGroupUpdatePacket implements DocumentPacket {

    private PermissionGroup group;

    @Override
    public String getChannel() {
        return "cloud_module_perms";
    }

    @Override
    public void handleData(BufferState state, Document document) {
        switch (state) {
            case READ:
                group = document.get("group").toInstance(DefaultPermissionGroup.class);
                break;

            case WRITE:
                document.set("group", group);
                break;
        }
    }
}
