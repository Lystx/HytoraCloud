package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.DocumentWrapper;
import cloud.hytora.document.wrapped.WrappedDocument;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.IOException;

@NoArgsConstructor @AllArgsConstructor @Getter
public class ProtocolDocument implements WrappedDocument, Bufferable {

	/**
	 * The wrapping document
	 */
	private Document targetDocument;

	@Override
	public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

		switch (state) {

			case READ:
				targetDocument = DocumentFactory.newJsonDocument(buf.readString());
				break;

			case WRITE:
				if (targetDocument == null) {
					buf.writeString("{}");
					return;
				}
				buf.writeString(targetDocument.asRawJsonString());
				break;
		}
	}

	@Override
	public DocumentWrapper<org.bson.Document> asBsonDocument() {
		return targetDocument.asBsonDocument();
	}

	@Override
	public DocumentWrapper<Gson> asGsonDocument() {
		return targetDocument.asGsonDocument();
	}

	@Override
	public Object getFallbackValue() {
		return targetDocument.getFallbackValue();
	}

	@Override
	public Document fallbackValue(Object value) {
		return targetDocument.fallbackValue(value);
	}
}
