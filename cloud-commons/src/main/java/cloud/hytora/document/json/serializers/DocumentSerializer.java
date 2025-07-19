package cloud.hytora.document.json.serializers;

import cloud.hytora.document.Document;
import cloud.hytora.document.json.JsonDocument;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.JsonSerializer;
import cloud.hytora.simplejson.elements.JsonEntity;

import java.lang.reflect.Field;

public class DocumentSerializer extends JsonSerializer<Document> {

    @Override
    public Document deserialize(JsonEntity jsonElement, Field field, Json json, Class<?>... arguments) {
        if (jsonElement != null && jsonElement.isJsonObject()) {
            return new JsonDocument(jsonElement.asJsonObject());
        } else {
            return null;
        }
    }

    @Override
    public JsonEntity serialize(Document document, Json json, Field field) {
        if (document instanceof JsonDocument) {
            JsonDocument jsonDocument = (JsonDocument) document;
            return jsonDocument.getJsonObject();
        } else {
            System.out.println("Type => " + document);
        }


        JsonDocument jsonDocument = new JsonDocument(document.values());
        return jsonDocument.getJsonObject();
    }
}
