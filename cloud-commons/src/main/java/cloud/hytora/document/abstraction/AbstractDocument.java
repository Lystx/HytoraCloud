package cloud.hytora.document.abstraction;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.DocumentWrapper;
import cloud.hytora.document.bson.BsonDocument;
import cloud.hytora.document.gson.GsonDocument;
import cloud.hytora.document.IEntry;
import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;


public abstract class AbstractDocument implements Document {

	protected final AtomicBoolean editable;

	public AbstractDocument(boolean editable) {
		this(new AtomicBoolean(editable));
	}

	public AbstractDocument(@Nonnull AtomicBoolean editable) {
		this.editable = editable;
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return new GsonDocument(toMap()).toInstance(classOfT);
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean canEdit() {
		return editable.get();
	}

	private Object fallbackValue;

	@Override
	public Object getFallbackValue() {
		return fallbackValue;
	}

	@Override
	public Document fallbackValue(Object value) {
		this.fallbackValue = value;
		return this;
	}

	@Nonnull
	@Override
	public Document markUneditable() {
		editable.set(false);
		return this;
	}

	@Nonnull
	@Override
	public Document set(@Nonnull String path, @Nullable Object value) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		set0(path, value);
		return this;
	}

	@Nonnull
	@Override
	public Document set(@Nonnull Object values) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		DocumentFactory.newJsonDocument(values).forEach(this::set0);
		return this;
	}

	protected abstract void set0(@Nonnull String path, @Nullable Object value);

	@Nonnull
	@Override
	public Document remove(@Nonnull String path) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		remove0(path);
		return this;
	}

	protected abstract void remove0(@Nonnull String path);

	@Nonnull
	@Override
	public Document clear() {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		clear0();
		return this;
	}

	protected abstract void clear0();

	@Override
	public DocumentWrapper<org.bson.Document> asBsonDocument() {
		if (this instanceof BsonDocument) {
			return (DocumentWrapper<org.bson.Document>) this;
		}
		return null;
	}

	@Override
	public DocumentWrapper<Gson> asGsonDocument() {
		if (this instanceof GsonDocument) {
			return (DocumentWrapper<Gson>) this;
		}
		return null;
	}

	@Nonnull
	@Override
	public Collection<Object> values() {
		return toMap().values();
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return toEntryMap().values();
	}

	@Override
	public void forEach(@Nonnull BiConsumer<? super String, ? super Object> action) {
		toMap().forEach(action);
	}

	@Override
	public void forEachEntry(@Nonnull BiConsumer<? super String, ? super IEntry> action) {
		toEntryMap().forEach(action);
	}
}
