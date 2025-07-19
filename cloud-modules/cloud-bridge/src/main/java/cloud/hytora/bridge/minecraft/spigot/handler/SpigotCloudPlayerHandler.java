package cloud.hytora.bridge.minecraft.spigot.handler;

import cloud.hytora.bridge.minecraft.spigot.utils.LoggedCloudPlayer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.common.PublishingType;
import cloud.hytora.driver.event.defaults.player.CloudEventPlayerCommand;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.ConnectionType;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayer;

import java.util.UUID;

public class SpigotCloudPlayerHandler implements PacketHandler<PacketCloudEntityPlayer> {
    @Override
    public void handle(PacketChannel channel, PacketCloudEntityPlayer packet) {

        PacketBuffer buffer = packet.buffer();

        PacketCloudEntityPlayer.PayLoad payLoad = packet.getPayLoad();
        if (payLoad == PacketCloudEntityPlayer.PayLoad.PLAYER_COMMAND_EXECUTE) {

            UUID uuid = buffer.readUniqueId();
            String commandLine = buffer.readString();
            commandLine = commandLine.replaceFirst(" ", "");


            CloudPlayer cachedCloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(uuid);
            if (cachedCloudPlayer == null) {
                System.out.println("tried executing command for nulled player");
                return;
            }


            System.out.println("[Command] Simulating Player[name=" + cachedCloudPlayer.getName() + " uuid=" + uuid + "] executing command '" + commandLine + "'...");
            CloudPlayer logger = new LoggedCloudPlayer(cachedCloudPlayer, message -> {

                String runningNodeName = CloudDriver.getInstance().getServiceManager().thisService().getRunningNodeName();
                CloudDriver.getInstance().logToExecutor(NetworkComponent.of(runningNodeName, ConnectionType.NODE), "§8=> §f" + message);
            });

            CloudDriver
                    .getInstance()
                    .getEventManager()
                    .callEvent(new CloudEventPlayerCommand(logger, commandLine, true), PublishingType.INTERNAL);

            channel.sendResponse().setState(NetworkResponseState.OK).execute(packet);

        }
    }
}
