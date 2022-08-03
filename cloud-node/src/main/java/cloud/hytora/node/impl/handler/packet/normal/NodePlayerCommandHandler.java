package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import java.util.UUID;

public class NodePlayerCommandHandler implements PacketHandler<CloudPlayerExecuteCommandPacket> {
    @Override
    public void handle(PacketChannel wrapper, CloudPlayerExecuteCommandPacket packet) {
        String commandLine = packet.getCommandLine();
        UUID uuid = packet.getUuid();
        CloudDriver.getInstance().getPlayerManager().getCloudPlayer(uuid).ifPresent(cloudPlayer -> {

            CloudDriver.getInstance().getLogger().debug("Player [name={}, uuid={}] executed CloudSided-Ingame-command: '{}'", cloudPlayer.getName(), cloudPlayer.getUniqueId(), commandLine);
            CloudDriver.getInstance().getCommandManager().executeCommand(cloudPlayer, commandLine);
        });
    }
}
