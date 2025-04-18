package cloud.hytora.modules.global.packets;

import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.message.DocumentPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.permission.PermissionGroup;
import cloud.hytora.driver.permission.PermissionManager;
import cloud.hytora.driver.permission.PermissionPlayer;
import cloud.hytora.modules.global.impl.DefaultPermissionGroup;
import cloud.hytora.modules.global.impl.DefaultPermissionPlayer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PermsUpdatePlayerPacket implements DocumentPacket {

    @Override
    public String getChannel() {
        return "cloud_module_perms";
    }

    private PermissionPlayer player;

    @Override
    public void handleData(BufferState state, Document document) {

        switch (state) {
            case READ:
                player = document.get("player").toInstance(DefaultPermissionPlayer.class);
                break;

            case WRITE:
                document.set("player", player);
                break;
        }
    }
}
