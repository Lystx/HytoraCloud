package cloud.hytora.node.config;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.task.Task;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.packets.defaults.StorageUpdatePacket;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import cloud.hytora.node.NodeDriver;
import lombok.Getter;

import javax.annotation.Nonnull;

@Getter
public class NodeNetworkDocumentStorage implements INetworkDocumentStorage {

	/**
	 * The raw data of this config stored in a document
	 */
	private Document rawData;

	public NodeNetworkDocumentStorage() {
		Logger.constantInstance().trace("Initializing NetworkDocumentStorage (Node-Side)...");
	}

	@Nonnull
	@Override
	public INetworkDocumentStorage setRawData(@Nonnull Document data) {
		rawData = data;
		update();
		return this;
	}

	@Override
	public void update() {
		NodeDriver.getInstance().getNetworkExecutor().sendPacketToAll(new StorageUpdatePacket(StorageUpdatePacket.StoragePayLoad.UPDATE, rawData));
	}

	@Override
	public void fetch() {
		rawData = DocumentFactory.newJsonDocument();
		Logger.constantInstance().trace("DriverStorage is now set up!");
	}

	@Override
	public Task<Document> fetchAsync() {
		return Task.callAsync(() -> {
			fetch();
			return rawData;
		});
	}
}
