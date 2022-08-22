package cloud.hytora.node.impl.handler.packet.normal;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;

import java.util.UUID;

public class NodePlayerCommandHandler implements PacketHandler<CloudPlayerExecuteCommandPacket> {
    @Override
    public void handle(PacketChannel wrapper, CloudPlayerExecuteCommandPacket packet) {
        String commandLine = packet.getCommandLine();
        UUID uuid = packet.getUuid();
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayer(uuid).ifPresent(cloudPlayer -> {

            CloudDriver.getInstance().getLogger().debug("Player [name={}, uuid={}] executed CloudSided-Ingame-command: '{}'", cloudPlayer.getName(), cloudPlayer.getUniqueId(), commandLine);
            CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).executeCommand(commandLine.split(" "), new PlayerCommandContext(cloudPlayer));
        });
    }
}
