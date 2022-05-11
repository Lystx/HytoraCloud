package cloud.hytora.node.impl.config;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.packets.StorageUpdatePacket;
import cloud.hytora.driver.storage.DriverStorage;
import cloud.hytora.node.NodeDriver;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class NodeDriverStorage implements DriverStorage {

	/**
	 * The raw data of this config stored in a document
	 */
	private Document rawData;

	@Nonnull
	@Override
	public DriverStorage setRawData(@Nonnull Document data) {
		rawData = data;
		update();
		return this;
	}

	@Override
	public void update() {
		NodeDriver.getInstance().getExecutor().sendPacketToAll(new StorageUpdatePacket(StorageUpdatePacket.StoragePayLoad.UPDATE, rawData));
	}

	@Override
	public void fetch() {
		rawData = DocumentFactory.newJsonDocument();
	}
}
