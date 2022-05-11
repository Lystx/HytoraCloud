package cloud.hytora.driver.player;

import cloud.hytora.driver.common.SelfCloneable;
import cloud.hytora.driver.networking.packets.player.CloudPlayerKickPacket;
import cloud.hytora.driver.networking.packets.player.CloudPlayerSendServicePacket;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.services.CloudServer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface CloudPlayer extends Bufferable, SelfCloneable<CloudPlayer> {

    /**
     * @return the username of the player
     */
    @NotNull String getUsername();

    /**
     * @return the unique id of the player
     */
    @NotNull UUID getUniqueId();

    /**
     * @return the proxy server who the player is
     */
    CloudServer getProxyServer();

    /**
     * sets the proxy server of the player
     *
     * @param service the service to set
     */
    void setProxyServer(@NotNull CloudServer service);

    /**
     * @return the server who the player is
     */
    CloudServer getServer();

    /**
     * sets the server of the player
     *
     * @param service the service to set
     */
    void setServer(@NotNull CloudServer service);

    /**
     * connects the player to a service
     *
     * @param service the service to connect
     */
    default void connect(@NotNull CloudServer service) {
        assert getProxyServer() != null;
        this.getProxyServer().sendPacket(new CloudPlayerSendServicePacket(getUniqueId(), service.getName()));
    }

    /**
     * kicks the player
     */
    default void kick() {
        kick("");
    }

    /**
     * kicks the player with a reason
     *
     * @param reason the reason of the kick
     */
    default void kick(@NotNull String reason) {
        assert getProxyServer() != null;
        this.getProxyServer().sendPacket(new CloudPlayerKickPacket(getUniqueId(), getProxyServer().getName(), reason));
    }

    /**
     * updates the properties of the player
     */
    void update();

}
