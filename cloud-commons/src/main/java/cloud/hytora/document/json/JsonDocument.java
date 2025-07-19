package cloud.hytora.document.json;

import cloud.hytora.document.*;
import cloud.hytora.document.abstraction.AbstractDocument;
import cloud.hytora.simplejson.api.Json;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.elements.JsonArray;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.object.JsonObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Reader;
import java.io.Writer;
import java.util.*;


public class JsonDocument extends AbstractDocument implements DocumentWrapper<Json> {

	protected JsonObject object;

	public JsonDocument() {
		this(new JsonObject());
	}

	public JsonDocument(@Nonnull Reader reader) {
		this(JsonHelper.DEFAULT_JSON.fromJson(reader, JsonObject.class));
	}

	public JsonDocument(@Nonnull JsonObject object) {
		this(object, true);
	}

	public JsonDocument(@Nullable Object value) {
		this(JsonHelper.DEFAULT_JSON.toJson(value).asJsonObject());
	}

	public JsonDocument(@Nonnull String json) {
		this(JsonHelper.DEFAULT_JSON.fromJson(json, JsonObject.class));
	}

	public JsonDocument(@Nonnull Map<String, Object> values) {
		this();
		JsonHelper.setDocumentProperties(JsonHelper.DEFAULT_JSON, object, values);
	}

	public JsonDocument(@Nonnull JsonObject object, boolean editable) {
		super(editable);
		this.object = object;
	}

	@Nonnull
	@Override
	public Map<String, Object> toMap() {

		Map<String, Object> map = new HashMap<>();
		for (cloud.hytora.simplejson.elements.object.JsonEntry entry : object) {
			map.put(entry.getName(), new JsonEntry(entry.getValue()).toObject());
		}
		return map;
	}

	@Nonnull
	@Override
	public Map<String, IEntry> toEntryMap() {
		Map<String, IEntry> map = new HashMap<>();
		for (cloud.hytora.simplejson.elements.object.JsonEntry entry : object) {
			map.put(entry.getName(), new JsonEntry(entry.getValue()));
		}
		return map;
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return JsonHelper.DEFAULT_JSON.fromJson(object, classOfT);
	}

	@Nonnull
	@Override
	public Collection<String> keys() {
		List<String> keys = new ArrayList<>();
		for (cloud.hytora.simplejson.elements.object.JsonEntry entry : object) {
			keys.add(entry.getName());
		}
		return keys;
	}

	@Override
	public int size() {
		return object.size();
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(object).toString(JsonFormat.RAW);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(object).toString(JsonFormat.SIMPLE);
	}

	@Override
	protected void set0(@Nonnull String path, @Nullable Object value) {
		setElement(path, value);
	}

	@Override
	protected void remove0(@Nonnull String path) {
		setElement(path, null);
	}

	@Override
	protected void clear0() {
		object = new JsonObject();
	}

	@Override
	public boolean contains(@Nonnull String path) {
		Optional<JsonEntity> element = getElement(path);
		return element.isPresent() && !element.get().isArray();
	}

	@Override
	public boolean has(String path) {
		return getElement(path).isPresent();
	}

	@Nonnull
	@Override
	public IEntry get(@Nonnull String path) {
		JsonEntity element = getElement(path).orElse(null);
		return new JsonEntry(element);
	}

	@Nonnull
	@Override
	public Document getDocument(@Nonnull String path) {
		JsonEntity element = getElement(path).orElse(null);
		if (element == null || element.isNull()) setElement(path, element = new JsonObject());
		if (!element.isJsonObject()) throw new IllegalStateException("Element at " + path + " " + element.getClass().getSimpleName() + " cannot be converted to a JsonObject");
		return new JsonDocument(element.asJsonObject());
	}

	@Nonnull
	@Override
	public Bundle getBundle(@Nonnull String path) {
		JsonEntity element = getElement(path).orElse(null);
		if (element == null || element.isNull()) setElement(path, element = new JsonArray());
		if (element.isArray()) return new JsonBundle(element.asJsonArray(), editable);

		JsonArray array = new JsonArray(1);
		array.add(element);
		setElement(path, element);
		return new JsonBundle(array);
	}

	@Nonnull
	protected Optional<JsonEntity> getElement(@Nonnull String path) {
		return getElement(path, object);
	}

	@Nonnull
	protected Optional<JsonEntity> getElement(@Nonnull String path, @Nonnull JsonObject object) {
		JsonEntity fullPathElement = object.get(path);
		if (fullPathElement != null) return Optional.of(fullPathElement);

		int index = path.indexOf('.');
		if (index == -1) return Optional.empty();

		String child = path.substring(0, index);
		String newPath = path.substring(index + 1);

		JsonEntity element = object.get(child);
		if (element == null || element.isNull()) return Optional.empty();

		return getElement(newPath, element.asJsonObject());
	}

	protected void setElement(@Nonnull String path, @Nullable Object value) {

		LinkedList<String> paths = determinePath(path);
		JsonObject object = this.object;

		for (int i = 0; i < paths.size() - 1; i++) {

			String current = paths.get(i);
			JsonEntity element = object.get(current);
			if (element == null || element.isNull()) {
				if (value == null) return; // There's noting to remove
				object.addProperty(current, element = new JsonObject());
			}

			if (!element.isObject()) {
				object = this.object;
				break;
			}
			object = element.asJsonObject();

		}

		String usePath = object == this.object ? path : paths.getLast();
		object.addProperty(usePath, JsonHelper.toJsonElement(value));
	}

	@Nonnull
	protected LinkedList<String> determinePath(@Nonnull String path) {
		LinkedList<String> paths = new LinkedList<>();
		String pathCopy = path;
		int index;
		while ((index = pathCopy.indexOf('.')) != -1) {
			String child = pathCopy.substring(0, index);
			pathCopy = pathCopy.substring(index + 1);
			paths.add(child);
		}
		paths.add(pathCopy);
		return paths;
	}

	@Nonnull
	public JsonObject getJsonObject() {
		return object;
	}


	@Nonnull
	@Override
	public Document set(@Nonnull Object values) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		Document.json(values).forEach(this::set0);
		return this;
	}

	@Override
	public void write(@Nonnull Writer writer) {
		try {
			JsonHelper.DEFAULT_JSON.toJson(object, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return this.asRawJsonString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		JsonDocument that = (JsonDocument) o;
		return Objects.equals(object, that.object);
	}

	@Override
	public int hashCode() {
		return Objects.hash(object);
	}

	@Override
	public Json getWrapper() {
		return JsonHelper.DEFAULT_JSON;
	}
}
