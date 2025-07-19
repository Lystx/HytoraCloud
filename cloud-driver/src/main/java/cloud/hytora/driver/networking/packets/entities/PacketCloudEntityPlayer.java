package cloud.hytora.driver.networking.packets.entities;

import cloud.hytora.driver.common.component.Component;
import cloud.hytora.driver.entity.player.connection.PlayerConnection;
import cloud.hytora.driver.entity.services.CloudService;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.driver.entity.player.CloudPlayer;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;


@Getter
public class PacketCloudEntityPlayer extends AbstractPacket {


    public static PacketCloudEntityPlayer forProxyLoginRequest(PlayerConnection connection, CloudService firstJoinServer) {
        return new PacketCloudEntityPlayer(PayLoad.PROXY_LOGIN_REQUEST, buf -> buf.writeObject(connection).writeString(firstJoinServer.getName()));
    }

    public static PacketCloudEntityPlayer forProxyLoginSuccess(UUID uniqueId, String proxy, String server, PlayerConnection connection) {
        return new PacketCloudEntityPlayer(PayLoad.PROXY_LOGIN_SUCCESS, buf -> buf.writeOptionalUniqueId(uniqueId).writeString(proxy).writeString(server).writeObject(connection));
    }

    public static PacketCloudEntityPlayer forProxyLoginFailed(UUID uniqueId, String proxy, String reason) {
        return new PacketCloudEntityPlayer(PayLoad.PROXY_LOGIN_FAILED, buf -> buf.writeOptionalUniqueId(uniqueId).writeString(proxy).writeString(reason));
    }

    public static PacketCloudEntityPlayer forProxyPlayerDisconnect(UUID uniqueId) {
        return new PacketCloudEntityPlayer(PayLoad.PROXY_PLAYER_DISCONNECT, buf -> buf.writeUniqueId(uniqueId));
    }


    public static PacketCloudEntityPlayer forServerConnected(UUID uniqueId, String serverName) {
        return new PacketCloudEntityPlayer(PayLoad.SERVER_CONNECTED, buf -> buf.writeOptionalUniqueId(uniqueId).writeString(serverName));
    }

    public static PacketCloudEntityPlayer forServerConnectedSuccess(UUID uniqueId, String serverName) {
        return new PacketCloudEntityPlayer(PayLoad.SERVER_CONNECTED_SUCCESS, buf -> buf.writeOptionalUniqueId(uniqueId).writeString(serverName));
    }

    public static PacketCloudEntityPlayer forPlayerCommandExecute(UUID uniqueId, String commandLine) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_COMMAND_EXECUTE, buf -> buf.writeUniqueId(uniqueId).writeString(commandLine));
    }

    public static PacketCloudEntityPlayer forPlayerTabComplete(UUID uniqueId, String commandLine) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_TAB_COMPLETE, buf -> buf.writeUniqueId(uniqueId).writeString(commandLine));
    }

    public static PacketCloudEntityPlayer forPlayerUpdate(CloudPlayer player) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_UPDATE, buf -> buf.writeObject(player));
    }

    public static PacketCloudEntityPlayer forPlayerKick(UUID playerId, String reason) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_EXECUTE_KICK, buf -> buf.writeUniqueId(playerId).writeOptionalString(reason));
    }
    public static PacketCloudEntityPlayer forPlayerPlainMessage(UUID playerId, String msg) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_EXECUTE_MESSAGE, buf -> buf.writeUniqueId(playerId).writeOptionalString(msg));
    }

    public static PacketCloudEntityPlayer forPlayerComponentMessage(UUID playerId, Component message) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_EXECUTE_MESSAGE, buf -> buf.writeUniqueId(playerId).writeObject(message));
    }

    public static PacketCloudEntityPlayer forPlayerTabList(UUID playerId, String header, String footer) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_EXECUTE_TAB_LIST, buf -> buf.writeUniqueId(playerId).writeString(header).writeString(footer));
    }
    public static PacketCloudEntityPlayer forPlayerSend(UUID playerId, String server) {
        return new PacketCloudEntityPlayer(PayLoad.PLAYER_EXECUTE_CONNECT, buf -> buf.writeUniqueId(playerId).writeString(server));
    }

    private PayLoad payLoad;
    
    public PacketCloudEntityPlayer() {
        super(buf -> buf.writeEnum(PayLoad.UNKNOWN));
    }
    public PacketCloudEntityPlayer(PayLoad payLoad, Consumer<PacketBuffer> buffer) {
        super(buffer);
        this.payLoad = payLoad;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case READ:
                this.payLoad = buf.readEnum(PayLoad.class);
                break;
            case WRITE:
                buf.writeEnum(payLoad);
                break;
        }
    }

    public enum PayLoad {

        UNKNOWN,

        PROXY_LOGIN_REQUEST,

        PROXY_LOGIN_SUCCESS,

        PROXY_LOGIN_FAILED,

        SERVER_CONNECTED_SUCCESS,

        SERVER_CONNECTED,

        PROXY_PLAYER_DISCONNECT,

        PLAYER_COMMAND_EXECUTE,

        PLAYER_TAB_COMPLETE,

        PLAYER_UPDATE,

        PLAYER_EXECUTE_KICK,

        PLAYER_EXECUTE_MESSAGE,

        PLAYER_EXECUTE_COMPONENT_MESSAGE,

        PLAYER_EXECUTE_CONNECT, PLAYER_EXECUTE_TAB_LIST

    }
}
