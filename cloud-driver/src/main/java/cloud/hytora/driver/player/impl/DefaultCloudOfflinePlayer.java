package cloud.hytora.driver.player.impl;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.document.Document;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.exception.PlayerNotOnlineException;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import cloud.hytora.driver.networking.protocol.packets.QueryState;
import cloud.hytora.driver.player.CloudOfflinePlayer;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.connection.DefaultPlayerConnection;
import cloud.hytora.driver.player.connection.PlayerConnection;
import lombok.*;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.UUID;


@NoArgsConstructor @Getter @AllArgsConstructor
@Setter
@ToString
public class DefaultCloudOfflinePlayer implements CloudOfflinePlayer {

	protected UUID uniqueId;
	protected String name;
	protected DefaultPlayerConnection lastConnection;
	protected long firstLogin;
	protected long lastLogin;
	protected Document properties;

	@Override
	public void applyBuffer(BufferState state, @NotNull PacketBuffer buffer) throws IOException {
		switch (state) {
			case WRITE:
				buffer.writeUniqueId(uniqueId);
				buffer.writeString(name);
				buffer.writeObject(lastConnection);
				buffer.writeLong(firstLogin);
				buffer.writeLong(lastLogin);
				buffer.writeDocument(properties);
				break;

			case READ:
				uniqueId = buffer.readUniqueId();
				name = buffer.readString();
				lastConnection = buffer.readObject(DefaultPlayerConnection.class);
				firstLogin = buffer.readLong();
				lastLogin = buffer.readLong();
				properties = buffer.readDocument();
				break;
		}
	}

	@Override
	public boolean isOnline() {
		return CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(this.uniqueId) != null;
	}

	@Override
	public CloudPlayer asOnlinePlayer() throws PlayerNotOnlineException {
		if (this.isOnline()) {
			return CloudDriver.getInstance().getPlayerManager().getCloudPlayerByUniqueIdOrNull(this.uniqueId);
		}
		throw new PlayerNotOnlineException();
	}

	@Override
	public void saveOfflinePlayer() {
		CloudDriver.getInstance().getPlayerManager().saveOfflinePlayerAsync(this);
	}

	@Nonnull
	@Override
	public Document getProperties() {
		return properties;
	}

	@Override
	public void setLastConnection(@NotNull PlayerConnection connection) {
		this.lastConnection = (DefaultPlayerConnection) connection;
	}

	@Override
	public String getMainIdentity() {
		return uniqueId.toString();
	}
}
