package cloud.hytora.document;

import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.document.bson.BsonDocument;
import cloud.hytora.document.empty.EmptyDocument;
import cloud.hytora.document.gson.GsonDocument;
import cloud.hytora.document.wrapped.StorableDocument;
import cloud.hytora.document.wrapped.WrappedDocument;
import com.google.gson.Gson;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

	@Nonnull
	@CheckReturnValue
	public static Document emptyDocument() {
		return EmptyDocument.INSTANCE;
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument() {
		return new GsonDocument();
	}

	@Nonnull
	@CheckReturnValue
	public static Document newBsonDocument() {
		return new BsonDocument();
	}

	@Nonnull
	@CheckReturnValue
	public static Document newBsonDocument(org.bson.Document bsonDocument) {
		return new BsonDocument(bsonDocument);
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull String json) {
		return new GsonDocument(json);
	}

	@Nonnull
	@CheckReturnValue
	public static Document newBsonDocument(@Nonnull String json) {
		return new BsonDocument(json);
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull Object value) {
		return new GsonDocument(value);
	}

	@Nullable
	@CheckReturnValue
	public static Document newJsonDocumentNullable(@Nullable Object object) {
		return object == null ? null : newJsonDocument(object);
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull String key, @Nonnull Object value) {
		return newJsonDocument().set(key, value);
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull Object... keysAndValues) {
		Document document = newJsonDocument();
		if (keysAndValues.length % 2 != 0)
			throw new IllegalArgumentException("Cannot create document of " + keysAndValues.length + " arguments");
		for (int i = 0; i < keysAndValues.length; i += 2) {
			document.set(String.valueOf(keysAndValues[i]), keysAndValues[i + 1]);
		}
		return document;
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull Reader reader) {
		return new GsonDocument(reader);
	}

	public static Document newJsonDocumentByURL(String urlString) throws Exception {
		URL url = new URL(urlString);
		InputStream inputStream = url.openStream();
		InputStreamReader inputStreamReader = new InputStreamReader(url.openStream());
		Document document = newJsonDocument(inputStreamReader);


		inputStreamReader.close();
		inputStream.close();
		return document;
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull InputStream input) {
		return newJsonDocument(new InputStreamReader(input, StandardCharsets.UTF_8));
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull Path file) throws IOException {
		if (Files.exists(file))
			return new GsonDocument(FileUtils.newBufferedReader(file));
		return new GsonDocument();
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocument(@Nonnull File file) throws IOException {
		if (file.exists())
			return new GsonDocument(FileUtils.newBufferedReader(file));
		return new GsonDocument();
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocumentUnchecked(@Nonnull Path file) {
		try {
			return newJsonDocument(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@CheckReturnValue
	public static Document newJsonDocumentUnchecked(@Nonnull File file) {
		try {
			return newJsonDocument(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@CheckReturnValue
	public static StorableDocument newStorableJsonDocument(@Nonnull Path file) throws IOException {
		return newStorableDocument(newJsonDocument(file), file);
	}

	@Nonnull
	@CheckReturnValue
	public static StorableDocument newStorableJsonDocumentUnchecked(@Nonnull Path file) {
		try {
			return newStorableJsonDocument(file);
		} catch (Exception ex) {
			throw new WrappedException(ex);
		}
	}

	@Nonnull
	@CheckReturnValue
	public static StorableDocument newStorableDocument(@Nonnull Document document, @Nonnull Path file) {
		class DocumentClass implements WrappedDocument, StorableDocument {
			@Nonnull
			public Path getPath() {
				return file;
			}

			@Nonnull
			public File getFile() {
				return file.toFile();
			}

			@Nonnull
			public Document getTargetDocument() {
				return document;
			}

			public void saveExceptionally() throws Exception {
				saveToFile(file);
			}

			@Nonnull
			public String toString() {
				return this.asRawJsonString();
			}

			@Override
			public DocumentWrapper<org.bson.Document> asBsonDocument() {
				return document.asBsonDocument();
			}

			@Override
			public DocumentWrapper<Gson> asGsonDocument() {
				return document.asGsonDocument();
			}

			@Override
			public Object getFallbackValue() {
				return getTargetDocument().getFallbackValue();
			}

			@Override
			public Document fallbackValue(Object value) {
				return getTargetDocument().fallbackValue(value);
			}
		}
		return new DocumentClass();
	}

	@Nonnull
	@CheckReturnValue
	public static StorableDocument newStorableDocument(@Nonnull Document document, @Nonnull File file) {
		return newStorableDocument(document, file.toPath());
	}


	@Nonnull
	@CheckReturnValue
	public static WrappedDocument newWrappedDocument(@Nonnull Document document, @Nullable Boolean overwriteEditable) {
		return new WrappedDocument() {
			@Nonnull
			public Document getTargetDocument() {
				return document;
			}

			@Override
			public DocumentWrapper<org.bson.Document> asBsonDocument() {
				return document.asBsonDocument();
			}

			@Override
			public DocumentWrapper<Gson> asGsonDocument() {
				return document.asGsonDocument();
			}

			public boolean canEdit() {
				return overwriteEditable != null ? overwriteEditable : WrappedDocument.super.canEdit();
			}

			@Override
			public Object getFallbackValue() {
				return getTargetDocument().getFallbackValue();
			}

			@Override
			public Document fallbackValue(Object value) {
				return getTargetDocument().fallbackValue(value);
			}

			@Nonnull
			public String toString() {
				return this.asRawJsonString();
			}
		};
	}

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
	IEntry get(@Nonnull String path);

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
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (String) getFallbackValue();
		}
		return entry.toString();
	}

	default String getString(@Nonnull String path, @Nullable String def) {
		return get(path).toString(def);
	}

	default Object getObject(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return getFallbackValue();
		}
		return entry.toObject();
	}

	default boolean getBoolean(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (Boolean) getFallbackValue();
		}
		return entry.toBoolean();
	}

	default boolean getBoolean(@Nonnull String path, boolean def) {
		return get(path).toBoolean(def);
	}

	default long getLong(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (Long) getFallbackValue();
		}
		return entry.toLong();
	}

	default long getLong(@Nonnull String path, long def) {
		return get(path).toLong(def);
	}

	default int getInt(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (int) getFallbackValue();
		}
		return entry.toInt();
	}

	default int getInt(@Nonnull String path, int def) {
		return get(path).toInt(def);
	}

	default short getShort(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (short) getFallbackValue();
		}
		return entry.toShort();
	}

	default short getShort(@Nonnull String path, short def) {
		return get(path).toShort(def);
	}

	default byte getByte(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (byte) getFallbackValue();
		}
		return entry.toByte();
	}

	default byte getByte(@Nonnull String path, byte def) {
		return get(path).toByte(def);
	}

	default float getFloat(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (float) getFallbackValue();
		}
		return entry.toFloat();
	}

	default float getFloat(@Nonnull String path, float def) {
		return get(path).toFloat(def);
	}

	default double getDouble(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (double) getFallbackValue();
		}
		return entry.toDouble();
	}

	default double getDouble(@Nonnull String path, double def) {
		return get(path).toDouble(def);
	}

	default UUID getUniqueId(@Nonnull String path) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (UUID) getFallbackValue();
		}
		return entry.toUniqueId();
	}

	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull Class<E> enumClass) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (E) getFallbackValue();
		}
		return entry.toEnum(enumClass);
	}

	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull E def) {
		return get(path).toEnum(def);
	}

	default <T> T getInstance(@Nonnull String path, @Nonnull Class<T> classOfT) {
		IEntry entry = get(path);
		if (entry.isNull() && getFallbackValue() != null) {
			this.set(path, getFallbackValue());
			return (T) getFallbackValue();
		}
		return entry.toInstance(classOfT);
	}

	default <T> T getInstance(@Nonnull String path, @Nonnull Type typeOfT) {
		IEntry entry = get(path);
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
