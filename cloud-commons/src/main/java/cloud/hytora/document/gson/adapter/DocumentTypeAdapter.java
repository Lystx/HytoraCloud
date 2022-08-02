package cloud.hytora.document.gson.adapter;

import cloud.hytora.document.gson.GsonDocument;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import cloud.hytora.document.Document;

import javax.annotation.Nonnull;
import java.io.IOException;


public class DocumentTypeAdapter implements GsonTypeAdapter<Document> {

	@Override
	public void write(@Nonnull Gson gson, @Nonnull JsonWriter writer, @Nonnull Document document) throws IOException {
		if (document instanceof GsonDocument) {
			GsonDocument gsonDocument = (GsonDocument) document;
			TypeAdapters.JSON_ELEMENT.write(writer, gsonDocument.getJsonObject());
			return;
		}


		GsonDocument gsonDocument = new GsonDocument(document.values());
		TypeAdapters.JSON_ELEMENT.write(writer, gsonDocument.getJsonObject());

	}

	@Override
	public Document read(@Nonnull Gson gson, @Nonnull JsonReader reader) throws IOException {
		JsonElement jsonElement = TypeAdapters.JSON_ELEMENT.read(reader);
		if (jsonElement != null && jsonElement.isJsonObject()) {
			return new GsonDocument(jsonElement.getAsJsonObject());
		} else {
			return null;
		}
	}

}
