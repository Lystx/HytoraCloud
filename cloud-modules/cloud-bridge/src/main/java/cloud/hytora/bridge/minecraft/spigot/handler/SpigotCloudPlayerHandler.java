package cloud.hytora.bridge.minecraft.spigot.handler;

import cloud.hytora.bridge.minecraft.spigot.utils.LoggedCloudPlayer;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.PublishingType;
import cloud.hytora.driver.event.defaults.player.CloudPlayerCommandEvent;
import cloud.hytora.driver.networking.NetworkComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.ConnectionType;
import cloud.hytora.driver.networking.protocol.packets.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.packet.PacketCloudPlayer;

import java.util.UUID;

public class SpigotCloudPlayerHandler implements PacketHandler<PacketCloudPlayer> {
    @Override
    public void handle(PacketChannel wrapper, PacketCloudPlayer packet) {

        PacketBuffer buffer = packet.buffer();

        PacketCloudPlayer.PayLoad payLoad = buffer.readEnum(PacketCloudPlayer.PayLoad.class);
        if (payLoad == PacketCloudPlayer.PayLoad.PLAYER_COMMAND_EXECUTE) {

            UUID uuid = buffer.readUniqueId();
            String commandLine = buffer.readString();
            commandLine = commandLine.replaceFirst(" ", "");


            ICloudPlayer cachedCloudPlayer = CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(uuid);
            if (cachedCloudPlayer == null) {
                System.out.println("tried executing command for nulled player");
                return;
            }


            System.out.println("[Command] Simulating Player[name=" + cachedCloudPlayer.getName() + " uuid=" + uuid + "] executing command '" + commandLine + "'...");
            ICloudPlayer logger = new LoggedCloudPlayer(cachedCloudPlayer, message -> {

                String runningNodeName = CloudDriver.getInstance().getServiceManager().thisService().getRunningNodeName();
                CloudDriver.getInstance().logToExecutor(NetworkComponent.of(runningNodeName, ConnectionType.NODE), "§8=> §f" + message);
            });

            CloudDriver
                    .getInstance()
                    .getEventManager()
                    .callEvent(new CloudPlayerCommandEvent(logger, commandLine, true), PublishingType.INTERNAL);

            wrapper.prepareResponse().state(NetworkResponseState.OK).execute(packet);

        }
    }
}
