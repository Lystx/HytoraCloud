package cloud.hytora.driver.event.defaults.player;

import cloud.hytora.common.task.Task;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.ProtocolTansferableEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.services.ICloudService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.UUID;

public class CloudPlayerChangeServerEvent implements  ProtocolTansferableEvent {

    @Nullable
    private UUID playerId;
    private String server;

    public CloudPlayerChangeServerEvent(ICloudPlayer cloudPlayer, ICloudService server) {
        this.playerId = cloudPlayer == null ? UUID.randomUUID() : cloudPlayer.getUniqueId();
        this.server = server == null ? "" : server.getName();
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

        switch (state) {
            case READ:
                playerId = buf.readUniqueId();
                server = buf.readString();
                break;
            case WRITE:
                buf.writeUniqueId(playerId);
                buf.writeString(server);
                break;
        }
    }


    public ICloudPlayer getPlayer() {
        return CloudDriver.getInstance().getPlayerManager().getCachedCloudPlayer(this.playerId);
    }

    public ICloudService getServer() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNull(this.server);
    }

    public Task<ICloudService> getCloudServerAsync() {
        return CloudDriver.getInstance().getServiceManager().getServiceByNameOrNullAsync(this.server);
    }
}
