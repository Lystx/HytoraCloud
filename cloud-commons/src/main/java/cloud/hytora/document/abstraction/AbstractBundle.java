package cloud.hytora.document.abstraction;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.IEntry;
import cloud.hytora.common.misc.CollectionUtils;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;


public abstract class AbstractBundle implements Bundle {

	protected AtomicBoolean editable;

	public AbstractBundle(@Nonnull AtomicBoolean editable) {
		this.editable = editable;
	}

	public AbstractBundle(boolean editable) {
		this(new AtomicBoolean(editable));
	}

	@Override
	public void forEach(Consumer<? super IEntry> action) {
		entries().forEach(action);
	}

	@NotNull
	@Override
	public Iterator<IEntry> iterator() {
		return entries().iterator();
	}

	@Override
	public Spliterator<IEntry> spliterator() {
		return entries().spliterator();
	}

	@Nonnull
	@Override
	public Object[] toArray() {
		return toList().toArray();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}

	@Override
	public boolean canEdit() {
		return editable.get();
	}

	@Nonnull
	@Override
	public Bundle markUneditable() {
		editable.set(false);
		return this;
	}

	@Nonnull
	@Override
	public Bundle set(int index, @Nullable Object value) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		if (index >= size()) throw new IllegalArgumentException("Index " + index + " out of bounds for size " + size());
		set0(index, value);
		return this;
	}

	protected abstract void set0(int index, @Nullable Object value);

	@Nonnull
	@Override
	public Bundle add(@Nullable Object value) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		add0(value);
		return this;
	}

	protected abstract void add0(@Nullable Object value);

	@Nonnull
	@Override
	public Bundle remove(int index) {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		if (index >= size()) throw new IllegalArgumentException("Index " + index + " out of bounds for size " + size());
		remove0(index);
		return this;
	}

	protected abstract void remove0(int index);

	@Nonnull
	@Override
	public Bundle clear() {
		if (!canEdit()) throw new IllegalStateException("Cannot be edited");
		clear0();
		return this;
	}

	protected abstract void clear0();

	@Nonnull
	@Override
	public IEntry getEntry(int index) {
		if (index >= size()) throw new IllegalArgumentException("Index " + index + " out of bounds for size " + size());
		return getEntry0(index);
	}

	protected abstract IEntry getEntry0(int index);


	@Nonnull
	protected <T> List<T> convertEntries(@Nonnull Function<IEntry, T> mapper) {
		return CollectionUtils.convertCollection(entries(), mapper);
	}

	@Override
	public void forEachEntry(@Nonnull Consumer<? super IEntry> action) {
		entries().forEach(action);
	}
}
