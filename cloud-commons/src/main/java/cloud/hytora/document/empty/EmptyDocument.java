package cloud.hytora.document.empty;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.Writer;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.BiConsumer;


public class EmptyDocument implements Document {

	public static final EmptyDocument INSTANCE = new EmptyDocument();

	public static final String JSON = "{}";

	private static final Map<String, Object> map = Collections.emptyMap();
	private static final Map<String, IEntry> entryMap = Collections.emptyMap();
	private static final Collection<String> keys = Collections.emptyList();
	private static final Collection<Object> values = Collections.emptyList();
	private static final Collection<IEntry> entries = Collections.emptyList();

	@Nonnull
	@Override
	public Map<String, Object> toMap() {
		return map;
	}

	@Nonnull
	@Override
	public Map<String, IEntry> toEntryMap() {
		return entryMap;
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return JSON;
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return JSON;
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return null;
	}

	@Override
	public int size() {
		return 0;
	}

	@Override
	public boolean isEmpty() {
		return true;
	}

	@Override
	public boolean contains(@Nonnull String path) {
		return false;
	}

	@Override
	public boolean has(String path) {
		return false;
	}

	@Nonnull
	@Override
	public Collection<String> keys() {
		return keys;
	}

	@Nonnull
	@Override
	public Collection<Object> values() {
		return values;
	}

	@Nonnull
	@Override
	public Collection<IEntry> entries() {
		return entries;
	}

	@Nonnull
	@Override
	public IEntry getEntry(@Nonnull String path) {
		return EmptyEntry.INSTANCE;
	}

	@Nonnull
	@Override
	public Bundle getBundle(@Nonnull String path) {
		return EmptyBundle.INSTANCE;
	}

	@Nonnull
	@Override
	public Document getDocument(@Nonnull String path) {
		return EmptyDocument.INSTANCE;
	}

	@Nonnull
	@Override
	public Document set(@Nonnull String path, @Nullable Object value) {
		throw new IllegalStateException("Cannot be edited");
	}

	@Nonnull
	@Override
	public Document set(@Nonnull Object values) {
		throw new IllegalStateException("Cannot be edited");
	}

	@Nonnull
	@Override
	public Document remove(@Nonnull String path) {
		throw new IllegalStateException("Cannot be edited");
	}

	@Nonnull
	@Override
	public Document clear() {
		throw new IllegalStateException("Cannot be edited");
	}

	@Override
	public boolean canEdit() {
		return false;
	}

	@Nonnull
	@Override
	public Document markUneditable() {
		return this;
	}

	@Override
	public Object getFallbackValue() {
		return null;
	}

	@Override
	public Document fallbackValue(Object value) {
		return null;
	}

	@Override
	public void forEach(@Nonnull BiConsumer<? super String, ? super Object> action) {
	}

	@Override
	public void forEachEntry(@Nonnull BiConsumer<? super String, ? super IEntry> action) {
	}

	@Override
	public void write(@Nonnull Writer writer) {
	}

	@Override
	public String toString() {
		return this.asRawJsonString();
	}
}
