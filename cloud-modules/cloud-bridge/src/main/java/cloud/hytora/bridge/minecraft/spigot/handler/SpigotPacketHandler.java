package cloud.hytora.bridge.minecraft.spigot.handler;

import cloud.hytora.driver.common.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.PacketHandler;
import cloud.hytora.driver.networking.protocol.wrapped.PacketChannel;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class SpigotPacketHandler implements PacketHandler<PacketCloudEntityPlayerExtension> {

    @Override
    public void handle(PacketChannel channel, PacketCloudEntityPlayerExtension packet) {
        PacketBuffer buffer = packet.buffer();

        switch (buffer.readEnum(PacketCloudEntityPlayerExtension.Type.class)) {
            case PROXY:
                break;
            case BUKKIT:
                PacketCloudEntityPlayerExtension.BukkitPayLoad bukkitPayLoad = buffer.readEnum(PacketCloudEntityPlayerExtension.BukkitPayLoad.class);
                UUID playerId = buffer.readUniqueId();
                switch (bukkitPayLoad) {
                    case GET_LOCATION:
                        Player player = Bukkit.getPlayer(playerId);
                        if (player == null) {
                            packet.sendResponse()
                                    .setState(NetworkResponseState.BAD_REQUEST)
                                    .setError(new PlayerNotOnlineException())
                                    .execute();
                            return;
                        }

                        Location location = player.getLocation();
                        packet.sendResponse()
                                .setState(NetworkResponseState.OK)
                                .setBuffer(buf -> {
                                    buf.writeDouble(location.getX());
                                    buf.writeDouble(location.getY());
                                    buf.writeDouble(location.getZ());
                                    buf.writeFloat(location.getYaw());
                                    buf.writeFloat(location.getPitch());
                                    buf.writeString(location.getWorld().getName());
                                })
                                .execute();
                        break;
                    case TELEPORT_LOCATION:
                        double x = buffer.readDouble();
                        double y = buffer.readDouble();
                        double z = buffer.readDouble();
                        float yaw = buffer.readFloat();
                        float pitch = buffer.readFloat();
                        String world = buffer.readString();
                        Location newLocation = new Location(
                                Bukkit.getWorld(world),
                                x,
                                y,
                                z,
                                yaw,
                                pitch
                        );
                        Player player1 = Bukkit.getPlayer(playerId);
                        if (player1 == null) {
                            return;
                        }
                        player1.teleport(newLocation);
                        break;
                }
                break;
        }
    }
}
