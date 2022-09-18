package cloud.hytora.document.gson.adapter;

import cloud.hytora.document.gson.GsonDocument;
import cloud.hytora.http.ProtocolAddress;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.internal.bind.TypeAdapters;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class AddressTypeAdapter implements GsonTypeAdapter<ProtocolAddress> {

    @Override
    public void write(@NotNull Gson gson, @NotNull JsonWriter writer, @NotNull ProtocolAddress object) throws IOException {

        JsonObject json = new JsonObject();

        json.addProperty("address", object.getHost());
        json.addProperty("port", object.getPort());
        if (object.getAuthKey() != null) {
            json.addProperty("authKey", object.getAuthKey());
        }

        TypeAdapters.JSON_ELEMENT.write(writer, json);
    }

    @Override
    public ProtocolAddress read(@NotNull Gson gson, @NotNull JsonReader reader) throws IOException {
        JsonObject object = gson.fromJson(reader, JsonObject.class);
        return new ProtocolAddress(
                object.get("address").getAsString(),
                object.get("port").getAsInt(),
                object.has("authKey") ? object.get("authKey").getAsString() : null
        );
    }
}
