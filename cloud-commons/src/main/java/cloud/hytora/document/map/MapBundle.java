package cloud.hytora.document.map;

import cloud.hytora.document.gson.GsonHelper;
import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.abstraction.AbstractBundle;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class MapBundle extends AbstractBundle {

	protected final List<Object> values;

	@SuppressWarnings("unchecked")
	public MapBundle(@Nonnull AtomicBoolean editable, @Nonnull Collection<?> values) {
		super(editable);
		this.values = values instanceof List ? (List<Object>) values : new ArrayList<>(values);
	}

	public MapBundle(@Nonnull Collection<?> values) {
		this(new AtomicBoolean(false), values);
	}

	public MapBundle() {
		this(new ArrayList<>());
	}

	@Nonnull
	@Override
	public List<Object> toList() {
		return Collections.unmodifiableList(values);
	}

	@Nonnull
	@Override
	public <T> List<T> toInstances(@Nonnull Class<T> classOfT) {
		return convertEntries(entry -> entry.toInstance(classOfT));
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return GsonHelper.DEFAULT_GSON.toJson(GsonHelper.toJsonElement(values));
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return GsonHelper.PRETTY_GSON.toJson(GsonHelper.toJsonElement(values));
	}

	@Override
	public int size() {
		return values.size();
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return CollectionUtils.convertCollection(values, MapEntry::new);
	}

	@Override
	protected void set0(int index, @Nullable Object value) {
		values.set(index, value);
	}

	@Override
	protected void add0(@Nullable Object value) {
		values.add(value);
	}

	@Override
	protected void remove0(int index) {
		values.remove(index);
	}

	@Override
	protected void clear0() {
		values.clear();
	}

	@Override
	protected IEntry getEntry0(int index) {
		return new MapEntry(values.get(0));
	}

	@Nonnull
	public Collection<?> getValues() {
		return values;
	}

	@Override
	public void write(@Nonnull Writer writer) {
		throw new UnsupportedOperationException("Cannot write a MapBundle");
	}
}
