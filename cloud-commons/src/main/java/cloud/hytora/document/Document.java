package cloud.hytora.document;

import cloud.hytora.common.misc.FileUtils;
import com.google.gson.Gson;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**

 *
 * @see Bundle
 * @see IEntry
 */
public interface Document extends JsonEntity {

	DocumentWrapper<org.bson.Document> asBsonDocument();

	DocumentWrapper<Gson> asGsonDocument();

	/**
	 * Returns an immutable map containing all values of this document.
	 * All values will be unpacked to java objects.
	 *
	 * @return the unpacked values of this document as immutable map
	 */
	@Nonnull
	Map<String, Object> toMap();

	/**
	 * Returns an immutable map containing all values of this document.
	 * All values will be wrapped as {@link IEntry}.
	 *
	 * @return the values of this document wrapped as {@link IEntry} as immutable map
	 */
	@Nonnull
	Map<String, IEntry> toEntryMap();

	/**
	 * @param classOfT the class this entry should be converted to
	 * @return this document converted to the given class, or {@code null} if this is {@link #isEmpty() empty}
	 *
	 * @throws IllegalStateException
	 *         If the value cannot be converted to the given instance class by the underlying library
	 *
	 * @see IEntry#toInstance(Class)
	 */
	<T> T toInstance(@Nonnull Class<T> classOfT);

	@Nonnull
	@Override
	String asRawJsonString();

	@Nonnull
	@Override
	String asFormattedJsonString();

	int size();

	boolean isEmpty();

	boolean contains(@Nonnull String path);

	boolean has(String path);

	/**
	 * @return the keys of this document as immutable collection
	 */
	@Nonnull
	Collection<String> keys();

	/**
	 * @return the unpacked values of this document as immutable collection
	 */
	@Nonnull
	Collection<Object> values();

	/**
	 * @return the values of this document wrapped as {@link IEntry} as immutable collection
	 */
	@Nonnull
	Collection<IEntry> entries();

	/**
	 * @param path the path of the target object
	 * @return the object at the given path wrapped as {@link IEntry}
	 */
	@Nonnull
	IEntry getEntry(@Nonnull String path);

	/**
	 * @param path the path of the target object
	 * @return the object at the given path as {@link Bundle} or a new created empty {@link Bundle} if {@code null}
	 *
	 * @throws IllegalStateException
	 *         If the object at the given path cannot be converted to a {@link Bundle}
	 */
	@Nonnull
	Bundle getBundle(@Nonnull String path);

	/**
	 * @param path the path of the target object
	 * @return the object at the given path as {@link Document} or a new created empty {@link Document} if {@code null}
	 *
	 * @throws IllegalStateException
	 *         If the object at the given path cannot be converted to a {@link Document}
	 */
	@Nonnull
	Document getDocument(@Nonnull String path);

	default String getString(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (String) getFallbackValue();
		}
		return entry.toString();
	}

	default String getString(@Nonnull String path, @Nullable String def) {
		return getEntry(path).toString(def);
	}

	default Object getObject(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return getFallbackValue();
		}
		return entry.toObject();
	}

	default boolean getBoolean(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (Boolean) getFallbackValue();
		}
		return entry.toBoolean();
	}

	default boolean getBoolean(@Nonnull String path, boolean def) {
		return getEntry(path).toBoolean(def);
	}

	default long getLong(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (Long) getFallbackValue();
		}
		return entry.toLong();
	}

	default long getLong(@Nonnull String path, long def) {
		return getEntry(path).toLong(def);
	}

	default int getInt(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (int) getFallbackValue();
		}
		return entry.toInt();
	}

	default int getInt(@Nonnull String path, int def) {
		return getEntry(path).toInt(def);
	}

	default short getShort(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (short) getFallbackValue();
		}
		return entry.toShort();
	}

	default short getShort(@Nonnull String path, short def) {
		return getEntry(path).toShort(def);
	}

	default byte getByte(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (byte) getFallbackValue();
		}
		return entry.toByte();
	}

	default byte getByte(@Nonnull String path, byte def) {
		return getEntry(path).toByte(def);
	}

	default float getFloat(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (float) getFallbackValue();
		}
		return entry.toFloat();
	}

	default float getFloat(@Nonnull String path, float def) {
		return getEntry(path).toFloat(def);
	}

	default double getDouble(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (double) getFallbackValue();
		}
		return entry.toDouble();
	}

	default double getDouble(@Nonnull String path, double def) {
		return getEntry(path).toDouble(def);
	}

	default UUID getUniqueId(@Nonnull String path) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (UUID) getFallbackValue();
		}
		return entry.toUniqueId();
	}

	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull Class<E> enumClass) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (E) getFallbackValue();
		}
		return entry.toEnum(enumClass);
	}

	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull E def) {
		return getEntry(path).toEnum(def);
	}

	default <T> T getInstance(@Nonnull String path, @Nonnull Class<T> classOfT) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (T) getFallbackValue();
		}
		return entry.toInstance(classOfT);
	}

	default <T> T getInstance(@Nonnull String path, @Nonnull Type typeOfT) {
		IEntry entry = getEntry(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (T) getFallbackValue();
		}
		return entry.toInstance(typeOfT);
	}

	@Nonnull
	default List<Document> getDocuments(@Nonnull String path) {
		return getBundle(path).toDocuments();
	}

	@Nonnull
	default List<Bundle> getBundles(@Nonnull String path) {
		return getBundle(path).toBundles();
	}

	@Nonnull
	default List<String> getStrings(@Nonnull String path) {
		return getBundle(path).toStrings();
	}

	@Nonnull
	default List<Boolean> getBooleans(@Nonnull String path) {
		return getBundle(path).toBooleans();
	}

	@Nonnull
	default List<Long> getLongs(@Nonnull String path) {
		return getBundle(path).toLongs();
	}

	@Nonnull
	default List<Integer> getInts(@Nonnull String path) {
		return getBundle(path).toInts();
	}

	@Nonnull
	default List<Short> getShorts(@Nonnull String path) {
		return getBundle(path).toShorts();
	}

	@Nonnull
	default List<Byte> getBytes(@Nonnull String path) {
		return getBundle(path).toBytes();
	}

	@Nonnull
	default List<Float> getFloats(@Nonnull String path) {
		return getBundle(path).toFloats();
	}

	default List<Double> getDoubles(@Nonnull String path) {
		return getBundle(path).toDoubles();
	}

	default List<UUID> getUniqueIds(@Nonnull String path) {
		return getBundle(path).toUniqueIds();
	}

	default <E extends Enum<?>> List<E> getEnums(@Nonnull String path, @Nonnull Class<E> enumClass) {
		return getBundle(path).toEnums(enumClass);
	}

	default <T> List<T> getInstances(@Nonnull String path, @Nonnull Class<T> classOfT) {
		return getBundle(path).toInstances(classOfT);
	}

	/**
	 * @return whether this document can be edited
	 */
	boolean canEdit();

	@Nonnull
	Document markUneditable();

	@Nonnull
	default Document referenceUneditable() {
		return DocumentFactory.newWrappedDocument(this, false);
	}

	Object getFallbackValue();

	Document fallbackValue(Object value);

	/**
	 * Sets the entry at given path to the given object.
	 * Setting something to {@code null} has the same effect as {@link #remove(String) removing} it.
	 *
	 * @param path the path of the target object
	 * @param value the new value
	 *
	 * @throws IllegalStateException
	 *         If this document cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Document set(@Nonnull String path, @Nullable Object value);

	/**
	 * Serializes the given object like in {@link #set(String, Object)}
	 * and applies the properties to this document.
	 *
	 * @param values the new values object
	 *
	 * @throws IllegalStateException
	 *         If this document cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Document set(@Nonnull Object values);

	/**
	 * Removes the entry at the given path.
	 * This has the same effect as {@link #set(String, Object) setting} it to {@code null}.
	 *
	 * @param path the path of the target object
	 *
	 * @throws IllegalStateException
	 *         If this document cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Document remove(@Nonnull String path);

	/**
	 * Clears all entries of this document
	 *
	 * @throws IllegalStateException
	 *         If this document cannot {@link #canEdit() be edited}
	 */
	@Nonnull
	Document clear();

	void forEach(@Nonnull BiConsumer<? super String, ? super Object> action);

	void forEachEntry(@Nonnull BiConsumer<? super String, ? super IEntry> action);

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

	@Nonnull
	default Document apply(@Nonnull Consumer<? super Document> action) {
		action.accept(this);
		return this;
	}

	@Nonnull
	default Document applyIf(boolean condition, @Nonnull Consumer<? super Document> action) {
		if (condition)
			action.accept(this);
		return this;
	}

}
