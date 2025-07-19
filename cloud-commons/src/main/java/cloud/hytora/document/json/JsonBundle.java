package cloud.hytora.document.json;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.abstraction.AbstractBundle;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.elements.JsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class JsonBundle extends AbstractBundle {

	protected JsonArray array;

	public JsonBundle() {
		this(new JsonArray());
	}

	public JsonBundle(@Nonnull Reader reader) {
		this(JsonHelper.DEFAULT_JSON.fromJson(reader, JsonArray.class));
	}

	public JsonBundle(int initialSize) {
		this(new JsonArray());
	}

	public JsonBundle(@Nonnull JsonArray array) {
		this(array, new AtomicBoolean(true));
	}

	public JsonBundle(@Nonnull JsonArray array, @Nonnull AtomicBoolean editable) {
		super(editable);
		this.array = array;
	}

	public JsonBundle(@Nonnull String json) {
		this(JsonHelper.DEFAULT_JSON.fromJson(json, JsonArray.class));
	}

	public JsonBundle(@Nonnull Collection<Object> objects) {
		this();
		addAll(objects);
	}

	@Nonnull
	@Override
	public List<Object> toList() {
		return CollectionUtils.convertIterator(array.iterator(), element -> new JsonEntry(element).toObject());
	}

	@Nonnull
	@Override
	public <T> List<T> toInstances(@Nonnull Class<T> classOfT) {
		return CollectionUtils.convertIterator(array.iterator(), element -> JsonHelper.DEFAULT_JSON.fromJson(element, classOfT));
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return CollectionUtils.convertIterator(array.iterator(), JsonEntry::new);
	}

	@Override
	public int size() {
		return array.size();
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(array).toString(JsonFormat.RAW);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(array).toString(JsonFormat.SIMPLE);
	}

	@Override
	protected void set0(int index, @Nullable Object value) {
		array.set(index, JsonHelper.toJsonElement(value));
	}

	@Override
	protected void add0(@Nullable Object value) {
		array.add(JsonHelper.toJsonElement(value));
	}

	@Override
	protected void remove0(int index) {
		array.remove(index);
	}

	@Override
	protected void clear0() {
		array = new JsonArray();
	}

	@Nonnull
	@Override
	public IEntry getEntry0(int index) {
		return new JsonEntry(array.get(index));
	}

	@Nonnull
	public JsonArray getJsonArray() {
		return array;
	}

	@Override
	public void write(@Nonnull Writer writer) {

		try {
			JsonHelper.DEFAULT_JSON.toJson(array, writer);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public String toString() {
		return asRawJsonString();
	}
}
