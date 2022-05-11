package cloud.hytora.document.gson;

import com.google.gson.JsonArray;
import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.abstraction.AbstractBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Reader;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class GsonBundle extends AbstractBundle {

	protected JsonArray array;

	public GsonBundle() {
		this(new JsonArray());
	}

	public GsonBundle(@Nonnull Reader reader) {
		this(GsonHelper.DEFAULT_GSON.fromJson(reader, JsonArray.class));
	}

	public GsonBundle(int initialSize) {
		this(new JsonArray(initialSize));
	}

	public GsonBundle(@Nonnull JsonArray array) {
		this(array, new AtomicBoolean(true));
	}

	public GsonBundle(@Nonnull JsonArray array, @Nonnull AtomicBoolean editable) {
		super(editable);
		this.array = array;
	}

	public GsonBundle(@Nonnull String json) {
		this(GsonHelper.DEFAULT_GSON.fromJson(json, JsonArray.class));
	}

	public GsonBundle(@Nonnull Collection<Object> objects) {
		this();
		addAll(objects);
	}

	@Nonnull
	@Override
	public List<Object> toList() {
		return CollectionUtils.convertIterator(array.iterator(), element -> new GsonEntry(element).toObject());
	}

	@Nonnull
	@Override
	public <T> List<T> toInstances(@Nonnull Class<T> classOfT) {
		return CollectionUtils.convertIterator(array.iterator(), element -> GsonHelper.DEFAULT_GSON.fromJson(element, classOfT));
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return CollectionUtils.convertIterator(array.iterator(), GsonEntry::new);
	}

	@Override
	public int size() {
		return array.size();
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return GsonHelper.DEFAULT_GSON.toJson(array);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return GsonHelper.PRETTY_GSON.toJson(array);
	}

	@Override
	protected void set0(int index, @Nullable Object value) {
		array.set(index, GsonHelper.toJsonElement(value));
	}

	@Override
	protected void add0(@Nullable Object value) {
		array.add(GsonHelper.toJsonElement(value));
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
		return new GsonEntry(array.get(index));
	}

	@Nonnull
	public JsonArray getJsonArray() {
		return array;
	}

	@Override
	public void write(@Nonnull Writer writer) {
		GsonHelper.PRETTY_GSON.toJson(array, writer);
	}

	@Override
	public String toString() {
		return asRawJsonString();
	}
}
