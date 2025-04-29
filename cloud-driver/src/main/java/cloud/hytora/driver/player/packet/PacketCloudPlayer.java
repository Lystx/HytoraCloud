package cloud.hytora.driver.player.packet;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.AbstractPacket;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.player.ICloudPlayer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.UUID;
import java.util.function.Consumer;


public class PacketCloudPlayer extends AbstractPacket {


    public static PacketCloudPlayer forProxyLoginRequest(UUID uniqueId, String name) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PROXY_LOGIN_REQUEST).writeOptionalUniqueId(uniqueId).writeOptionalString(name));
    }

    public static PacketCloudPlayer forProxyLoginSuccess(UUID uniqueId, String proxy, String server) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PROXY_LOGIN_SUCCESS).writeOptionalUniqueId(uniqueId).writeString(proxy).writeString(server));
    }

    public static PacketCloudPlayer forProxyLoginFailed(UUID uniqueId, String proxy, String reason) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PROXY_LOGIN_FAILED).writeOptionalUniqueId(uniqueId).writeString(proxy).writeString(reason));
    }

    public static PacketCloudPlayer forProxyPlayerDisconnect(UUID uniqueId) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PROXY_PLAYER_DISCONNECT).writeUniqueId(uniqueId));
    }


    public static PacketCloudPlayer forServerConnected(UUID uniqueId, String serverName) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.SERVER_CONNECTED).writeOptionalUniqueId(uniqueId).writeString(serverName));
    }

    public static PacketCloudPlayer forServerConnectedSuccess(UUID uniqueId, String serverName) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.SERVER_CONNECTED_SUCCESS).writeOptionalUniqueId(uniqueId).writeString(serverName));
    }

    public static PacketCloudPlayer forPlayerCommandExecute(UUID uniqueId, String commandLine) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_COMMAND_EXECUTE).writeUniqueId(uniqueId).writeString(commandLine));
    }

    public static PacketCloudPlayer forPlayerUpdate(ICloudPlayer player) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_UPDATE).writeObject(player));
    }

    public static PacketCloudPlayer forPlayerKick(UUID playerId, String reason) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_EXECUTE_KICK).writeUniqueId(playerId).writeOptionalString(reason));
    }
    public static PacketCloudPlayer forPlayerPlainMessage(UUID playerId, String msg) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_EXECUTE_MESSAGE).writeUniqueId(playerId).writeOptionalString(msg));
    }

    public static PacketCloudPlayer forPlayerComponentMessage(UUID playerId, Component message) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_EXECUTE_MESSAGE).writeUniqueId(playerId).writeObject(message));
    }

    public static PacketCloudPlayer forPlayerTabList(UUID playerId, String header, String footer) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_EXECUTE_TAB_LIST).writeUniqueId(playerId).writeString(header).writeString(footer));
    }
    public static PacketCloudPlayer forPlayerSend(UUID playerId, String server) {
        return new PacketCloudPlayer(buf -> buf.writeEnum(PayLoad.PLAYER_EXECUTE_CONNECT).writeUniqueId(playerId).writeString(server));
    }

    public PacketCloudPlayer() {
        super(buf -> buf.writeEnum(PayLoad.UNKNOWN));
    }
    public PacketCloudPlayer(Consumer<PacketBuffer> buffer) {
        super(buffer);
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
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

        PLAYER_UPDATE,

        PLAYER_EXECUTE_KICK,

        PLAYER_EXECUTE_MESSAGE,

        PLAYER_EXECUTE_COMPONENT_MESSAGE,

        PLAYER_EXECUTE_CONNECT, PLAYER_EXECUTE_TAB_LIST

    }
}
