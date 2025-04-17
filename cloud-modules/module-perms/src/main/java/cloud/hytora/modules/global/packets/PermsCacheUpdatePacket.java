package cloud.hytora.modules.global.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.message.DocumentPacket;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PermsCacheUpdatePacket implements DocumentPacket {

    private Collection<PermissionGroup> permissionGroups;


    @Override
    public String getChannel() {
        return "cloud_module_perms_test";
    }

    @Override
    public void handleData(BufferState state, Document document) {
        switch (state) {
            case WRITE:
                document.set("groups", permissionGroups);
                break;

            case READ:
                permissionGroups = new ArrayList<>(document.getBundle("groups").toInstances(DefaultPermissionGroup.class));
                break;
        }
    }
}
