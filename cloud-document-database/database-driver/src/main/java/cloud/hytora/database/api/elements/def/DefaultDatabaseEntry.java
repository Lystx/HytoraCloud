package cloud.hytora.database.api.elements.def;

import cloud.hytora.common.collection.IRandom;
import cloud.hytora.common.misc.RandomString;
import cloud.hytora.database.api.elements.DatabaseEntry;
import cloud.hytora.document.Document;
import cloud.hytora.document.gson.GsonDocument;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.Map;

public class DefaultDatabaseEntry extends GsonDocument implements DatabaseEntry {

    public DefaultDatabaseEntry() {
        this.setId(new RandomString(10).nextString());
    }

    public DefaultDatabaseEntry(String identification) {
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull Reader reader, String identification) {
        super(reader);
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull JsonObject object, String identification) {
        super(object);
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@Nullable Object value, String identification) {
        super(value);
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull String json, String identification) {
        super(json);
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull Document wrapped, String identification) {
        super(((GsonDocument)wrapped).getJsonObject());
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull Map<String, Object> values, String identification) {
        super(values);
        this.setId(identification);
    }

    public DefaultDatabaseEntry(@NotNull JsonObject object, boolean editable, String identification) {
        super(object, editable);
        this.setId(identification);
    }

    @Override
    public void setDocument(Document document) {
        super.object = ((GsonDocument)document).getJsonObject();
    }

    @Override
    public String getId() {
        return get("_id").toString();
    }

    @Override
    public void setId(String id) {
        set("_id", id);
    }

}
