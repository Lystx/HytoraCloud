package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;

import java.util.UUID;

public class NodePlayerCommandHandler implements PacketHandler<CloudPlayerExecuteCommandPacket> {
    @Override
    public void handle(PacketChannel wrapper, CloudPlayerExecuteCommandPacket packet) {
        String commandLine = packet.getCommandLine();
        UUID uuid = packet.getUuid();
        ICloudPlayer cachedCloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(uuid);
        if (cachedCloudPlayer != null) {
            CloudDriver.getInstance().getLogger().debug("Player [name={}, uuid={}] executed CloudSided-Ingame-command: '{}'", cachedCloudPlayer.getName(), cachedCloudPlayer.getUniqueId(), commandLine);
            CloudDriver.getInstance().getCommandManager().executeCommand(cachedCloudPlayer, commandLine);
        }
    }
}
