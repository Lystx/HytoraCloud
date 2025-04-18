package cloud.hytora.document;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.document.empty.EmptyBundle;
import cloud.hytora.document.gson.GsonBundle;
import cloud.hytora.document.wrapped.StorableBundle;
import cloud.hytora.document.wrapped.WrappedBundle;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * A Bundle represents an array or list of string, numbers, objects or arrays.
 *
 * @see Document
 * @see IEntry
 */
public interface Bundle extends JsonEntity, Iterable<IEntry> {

	@Nonnull
	@CheckReturnValue
	public static Bundle emptyBundle() {
		return EmptyBundle.INSTANCE;
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle() {
		return new GsonBundle();
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(int initialSize) {
		return new GsonBundle(initialSize);
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull String json) {
		return new GsonBundle(json);
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull Object... values) {
		return newJsonBundle().addAll(values);
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull Iterable<?> values) {
		return newJsonBundle().addAll(values);
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull Reader reader) {
		return new GsonBundle(reader);
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull InputStream input) {
		return newJsonBundle(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull Path file) throws IOException {
		if (Files.exists(file))
			return new GsonBundle(FileUtils.newBufferedReader(file));
		return new GsonBundle();
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundle(@Nonnull File file) throws IOException {
		if (file.exists())
			return new GsonBundle(FileUtils.newBufferedReader(file));
		return new GsonBundle();
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundleUnchecked(@Nonnull Path file) {
		try {
			return newJsonBundle(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@CheckReturnValue
	public static Bundle newJsonBundleUnchecked(@Nonnull File file) {
		try {
			return newJsonBundle(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@CheckReturnValue
	public static StorableBundle newStorableBundle(@Nonnull Bundle bundle, @Nonnull Path file) {
		class BundleClass implements WrappedBundle, StorableBundle {
			@Nonnull
			public Path getPath() {
				return file;
			}

			@Nonnull
			public File getFile() {
				return file.toFile();
			}

			@Nonnull
			public Bundle getTargetBundle() {
				return bundle;
			}

			public void saveExceptionally() throws Exception {
				saveToFile(file);
			}

			@Nonnull
			public String toString() {
				return this.asRawJsonString();
			}
		}
		return new BundleClass();
	}

	@Nonnull
	@CheckReturnValue
	public static StorableBundle newStorableBundle(@Nonnull Bundle bundle, @Nonnull File file) {
		return newStorableBundle(bundle, file.toPath());
	}


	@Nonnull
	@CheckReturnValue
	public static WrappedBundle newWrappedBundle(@Nonnull Bundle bundle, @Nullable Boolean overwriteEditable) {
		return new WrappedBundle() {
			@Nonnull
			public Bundle getTargetBundle() {
				return bundle;
			}

			public boolean canEdit() {
				return overwriteEditable != null ? overwriteEditable : WrappedBundle.super.canEdit();
			}

			@Nonnull
			public String toString() {
				return this.asRawJsonString();
			}
		};
	}

	@Nonnull
	@CheckReturnValue
	public static StorableBundle newStorableJsonBundle(@Nonnull Path file) throws IOException {
		return newStorableBundle(newJsonBundle(file), file);
	}

	@Nonnull
	@CheckReturnValue
	public static StorableBundle newStorableJsonBundleUnchecked(@Nonnull Path file) {
		try {
			return newStorableJsonBundle(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	Object[] toArray();

	@Nonnull
	List<Object> toList();


	default <T> List<T> toList(Class<T> typeClass) {
		return entries().stream().map(e -> e.toInstance(typeClass)).collect(Collectors.toList());
	}
	@Nonnull
	@Override
	String asRawJsonString();

	@Nonnull
	@Override
	String asFormattedJsonString();

	int size();

	boolean isEmpty();

	/**
	 * @return the values of this bundle wrapped as {@link IEntry} as immutable collection
	 */
	@Nonnull
	Collection<IEntry> entries();

	/**
	 * Returns the value at the given {@code index} as an {@link IEntry}
	 *
	 * @param index the target index
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code index} is out of bounds for the {@link #size()}
	 */
	@Nonnull
	IEntry getEntry(int index);

	/**
	 * Returns the value at the given {@code index} as a {@link Document}, cloud be {@code null}
	 *
	 * @param index the target index
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code index} is out of bounds for the {@link #size()}
	 * @throws IllegalStateException
	 *         If the element at the given {@code index} cannot be converted to a {@link Document}
	 */
	default Document getDocument(int index) {
		return getEntry(index).toDocument();
	}

	/**
	 * Returns the value at the given {@code index} as a {@link Bundle}, cloud be {@code null}
	 *
	 * @param index the target index
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code index} is out of bounds for the {@link #size()}
	 * @throws IllegalStateException
	 *         If the element at the given {@code index} cannot be converted to a {@link Bundle}
	 */
	default Bundle getBundle(int index) {
		return getEntry(index).toBundle();
	}

	/**
	 * Sets the value at the given index to the given value.
	 *
	 * @param index the index where to set the value
	 * @param value the new value
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code index} is out of bounds for the {@link #size()}
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Bundle set(int index, @Nullable Object value);

	/**
	 * Removes the given index from the bundle.
	 * The value will not be set to {@code null} but the index completely removed from the bundle.
	 * The {@link #size()} will then be 1 smaller.
	 *
	 * @param index the index to remove
	 *
	 * @throws IllegalArgumentException
	 *         If the {@code index} is out of bounds for the {@link #size()}
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Bundle remove(int index);

	/**
	 * Clears all entries of this bundle.
	 *
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Bundle clear();

	/**
	 * Adds the given value to the bundle.
	 * The {@link #size()} will then be 1 bigger.
	 *
	 * @param value the value to add
	 *
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Bundle add(@Nullable Object value);

	/**
	 * Adds the given values to the bundle.
	 *
	 * @param values the values to add
	 *
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	default Bundle addAll(@Nonnull Object... values) {
		for (Object value : values)
			add(value);
		return this;
	}

	/**
	 * Adds the given values to the bundle.
	 *
	 * @param values the values to add
	 *
	 * @throws IllegalStateException
	 *         If this bundle cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	default Bundle addAll(@Nonnull Iterable<?> values) {
		for (Object value : values)
			add(value);
		return this;
	}

	@Nonnull
	<T> List<T> toInstances(@Nonnull Class<T> classOfT);


	@Nonnull
	default List<String> toStrings() {
		return toInstances(String.class);
	}

	@Nonnull
	default List<Document> toDocuments() {
		return toInstances(Document.class);
	}

	@Nonnull
	default List<Bundle> toBundles() {
		return toInstances(Bundle.class);
	}

	@Nonnull
	default List<Long> toLongs() {
		return toInstances(long.class);
	}

	@Nonnull
	default List<Integer> toInts() {
		return toInstances(int.class);
	}

	@Nonnull
	default List<Short> toShorts() {
		return toInstances(short.class);
	}

	@Nonnull
	default List<Byte> toBytes() {
		return toInstances(byte.class);
	}

	@Nonnull
	default List<Double> toDoubles() {
		return toInstances(double.class);
	}

	@Nonnull
	default List<Float> toFloats() {
		return toInstances(float.class);
	}

	@Nonnull
	default List<Boolean> toBooleans() {
		return toInstances(boolean.class);
	}

	@Nonnull
	default List<UUID> toUniqueIds() {
		return toInstances(UUID.class);
	}

	@Nonnull
	default <E extends Enum<?>> List<E> toEnums(@Nonnull Class<E> enumClass) {
		return toInstances(enumClass);
	}

	/**
	 * @return whether this bundle can be edited
	 */
	boolean canEdit();

	@Nonnull
	Bundle markUneditable();

	@Nonnull
	default Bundle referenceUneditable() {
		return DocumentFactory.newWrappedBundle(this, false);
	}


	void forEachEntry(@Nonnull Consumer<? super IEntry> action);

	void write(@Nonnull Writer writer);

	default void saveToFile(@Nonnull Path file) throws IOException {
		BufferedWriter writer = FileUtils.newBufferedWriter(file);
		write(writer);
		writer.flush();
		writer.close();
	}

	default void saveToFile(@Nonnull File file) throws IOException {
		BufferedWriter writer = FileUtils.newBufferedWriter(file);
		write(writer);
		writer.flush();
		writer.close();
	}

}
