package cloud.hytora.node.impl.player.extension;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.common.location.impl.CloudLocation;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.player.extension.CloudBukkitPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.entity.services.NodeSpecificCloudService;
import cloud.hytora.driver.networking.packets.entities.PacketCloudEntityPlayerExtension;
import cloud.hytora.driver.networking.packets.response.BufferedResponse;
import cloud.hytora.driver.networking.packets.response.NetworkResponseState;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class NodeBukkitPlayer implements CloudBukkitPlayer {


    private final CloudPlayer cloudPlayer;

    @Override
    public void sendMessage(String message) {
        cloudPlayer.asProxyPlayer().sendMessage(message);
    }

    @Override
    public CloudLocation getLocation() {
        CloudService server = cloudPlayer.getServer();
        NodeSpecificCloudService nodeServer = (NodeSpecificCloudService) server;


        BufferedResponse response = nodeServer
                .getChannel()
                .sendQuery()
                .execute(PacketCloudEntityPlayerExtension.forBukkit(PacketCloudEntityPlayerExtension.BukkitPayLoad.GET_LOCATION, buf -> buf.writeUniqueId(this.cloudPlayer.getUniqueId())))
                .syncUninterruptedly()
                .get();
        PacketBuffer buffer = response.buffer();
        if (response.state() != NetworkResponseState.OK) {
            //query not ok
            return new CloudLocation(0D, 0D, 0D, "unknown");
        }
        return new CloudLocation(
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readDouble(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readString()
        );
    }

    @Override
    public void teleport(CloudLocation location) {
        if (cloudPlayer.getServer() == null) {
            return;
        }
        cloudPlayer.getServer()
                .sendPacket(PacketCloudEntityPlayerExtension.forBukkit(PacketCloudEntityPlayerExtension.BukkitPayLoad.TELEPORT_LOCATION, buffer -> {
                    buffer.writeUniqueId(this.cloudPlayer.getUniqueId()); //player identity

                    //location value
                    buffer.writeDouble(location.getX());
                    buffer.writeDouble(location.getY());
                    buffer.writeDouble(location.getZ());
                    buffer.writeFloat(location.getYaw());
                    buffer.writeFloat(location.getPitch());
                    buffer.writeString(location.getWorld());
                }));
    }
}
