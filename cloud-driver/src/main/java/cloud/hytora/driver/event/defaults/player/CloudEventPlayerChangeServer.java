package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.NetworkEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.player.CloudPlayer;
import cloud.hytora.driver.entity.services.CloudService;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Getter
public class CloudEventPlayerChangeServer implements NetworkEvent {

    private CloudPlayer player;
    private String server;

    public CloudEventPlayerChangeServer(CloudPlayer cloudPlayer, CloudService server) {
        this.player = cloudPlayer;
        this.server = server == null ? "" : server.getName();
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {
            case READ:
                player = buf.readPlayer();
                server = buf.readString();
                break;
            case WRITE:
                buf.writePlayer(player);
                buf.writeString(server);
                break;
        }
    }


    public CloudService getServer() {
        return CloudDriver.getInstance().getServiceManager().getCachedCloudService(this.server);
    }

}
