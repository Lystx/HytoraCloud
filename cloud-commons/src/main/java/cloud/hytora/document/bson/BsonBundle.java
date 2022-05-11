package cloud.hytora.document.bson;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.document.IEntry;
import cloud.hytora.document.abstraction.AbstractBundle;
import org.bson.BsonArray;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;


public class BsonBundle extends AbstractBundle {

	protected final BsonArray array;

	public BsonBundle(@Nonnull BsonArray array, @Nonnull AtomicBoolean editable) {
		super(editable);
		this.array = array;
	}

	@Nonnull
	@Override
	public List<Object> toList() {
		return convertEntries(IEntry::toObject);
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return BsonHelper.toJson(array, false);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return BsonHelper.toJson(array, true);
	}

	@Override
	public int size() {
		return array.size();
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return CollectionUtils.convertCollection(array, BsonEntry::new);
	}

	@Nonnull
	@Override
	public <T> List<T> toInstances(@Nonnull Class<T> classOfT) {
		return convertEntries(entry -> entry.toInstance(classOfT));
	}

	@Override
	public void write(@Nonnull Writer writer) {
		BsonHelper.toJson(array, true, writer);
	}

	@Override
	protected void set0(int index, @Nullable Object value) {
		array.set(index, BsonHelper.toBsonValue(value));
	}

	@Override
	protected void add0(@Nullable Object value) {
		array.add(BsonHelper.toBsonValue(value));
	}

	@Override
	protected void remove0(int index) {
		array.remove(index);
	}

	@Override
	protected void clear0() {
		array.clear();
	}

	@Override
	protected IEntry getEntry0(int index) {
		return null;
	}

	@Override
	public String toString() {
		return this.asRawJsonString();
	}
}
