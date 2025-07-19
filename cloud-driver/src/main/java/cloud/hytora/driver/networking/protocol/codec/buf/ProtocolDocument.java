package cloud.hytora.driver.networking.protocol.codec.buf;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentWrapper;
import cloud.hytora.document.wrapped.WrappedDocument;
import cloud.hytora.driver.networking.protocol.types.BufferState;
import cloud.hytora.simplejson.api.Json;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@NoArgsConstructor @AllArgsConstructor @Getter
public class ProtocolDocument implements WrappedDocument, IBufferObject {

	/**
	 * The wrapping document
	 */
	private Document targetDocument;

	@Override
	public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {

		switch (state) {

			case READ:
				targetDocument = Document.gson(buf.readString());
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
	public DocumentWrapper<org.bson.Document> asBson() {
		return targetDocument.asBson();
	}

	@Override
	public DocumentWrapper<Gson> asGson() {
		return targetDocument.asGson();
	}

	@Override
	public DocumentWrapper<Json> asJson() {
		return targetDocument.asJson();
	}

	@Override
	public Object getFallbackValue() {
		return targetDocument.getFallbackValue();
	}

	@Override
	public String toString() {
		return asRawJsonString();
	}

	@Override
	public Document fallbackValue(Object value) {
		return targetDocument.fallbackValue(value);
	}
}
