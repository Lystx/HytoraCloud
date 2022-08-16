package cloud.hytora.document.wrapped;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public interface WrappedDocument extends Document {

	@Nonnull
	Document getTargetDocument();

	@Override
	default boolean has(String path) {
		return getTargetDocument().has(path);
	}

	@Nonnull
	@Override
	default Map<String, Object> toMap() {
		return getTargetDocument().toMap();
	}

	@Nonnull
	@Override
	default Map<String, IEntry> toEntryMap() {
		return getTargetDocument().toEntryMap();
	}

	@Override
	default <T> T toInstance(@Nonnull Class<T> classOfT) {
		return getTargetDocument().toInstance(classOfT);
	}

	@Nonnull
	@Override
	default String asRawJsonString() {
		return getTargetDocument().asRawJsonString();
	}

	@Nonnull
	@Override
	default String asFormattedJsonString() {
		return getTargetDocument().asFormattedJsonString();
	}

	@Override
	default int size() {
		return getTargetDocument().size();
	}

	@Override
	default boolean isEmpty() {
		return getTargetDocument().isEmpty();
	}

	@Override
	default boolean contains(@Nonnull String path) {
		return getTargetDocument().contains(path);
	}

	@Nonnull
	@Override
	default Collection<String> keys() {
		return getTargetDocument().keys();
	}

	@Nonnull
	@Override
	default Collection<Object> values() {
		return getTargetDocument().values();
	}

	@Nonnull
	@Override
	default Collection<IEntry> entries() {
		return getTargetDocument().entries();
	}

	@Nonnull
	@Override
	default IEntry get(@Nonnull String path) {
		return getTargetDocument().get(path);
	}

	@Nonnull
	@Override
	default Bundle getBundle(@Nonnull String path) {
		return getTargetDocument().getBundle(path);
	}

	@Nonnull
	@Override
	default Document getDocument(@Nonnull String path) {
		return getTargetDocument().getDocument(path);
	}

	@Override
	default String getString(@Nonnull String path) {
		return getTargetDocument().getString(path);
	}

	@Override
	default String getString(@Nonnull String path, @Nullable String def) {
		return getTargetDocument().getString(path, def);
	}

	@Override
	default Object getObject(@Nonnull String path) {
		return getTargetDocument().getObject(path);
	}

	@Override
	default boolean getBoolean(@Nonnull String path) {
		return getTargetDocument().getBoolean(path);
	}

	@Override
	default boolean getBoolean(@Nonnull String path, boolean def) {
		return getTargetDocument().getBoolean(path, def);
	}

	@Override
	default long getLong(@Nonnull String path) {
		return getTargetDocument().getLong(path);
	}

	@Override
	default long getLong(@Nonnull String path, long def) {
		return getTargetDocument().getLong(path, def);
	}

	@Override
	default int getInt(@Nonnull String path) {
		return getTargetDocument().getInt(path);
	}

	@Override
	default int getInt(@Nonnull String path, int def) {
		return getTargetDocument().getInt(path, def);
	}

	@Override
	default short getShort(@Nonnull String path) {
		return getTargetDocument().getShort(path);
	}

	@Override
	default short getShort(@Nonnull String path, short def) {
		return getTargetDocument().getShort(path, def);
	}

	@Override
	default byte getByte(@Nonnull String path) {
		return getTargetDocument().getByte(path);
	}

	@Override
	default byte getByte(@Nonnull String path, byte def) {
		return getTargetDocument().getByte(path, def);
	}

	@Override
	default float getFloat(@Nonnull String path) {
		return getTargetDocument().getFloat(path);
	}

	@Override
	default float getFloat(@Nonnull String path, float def) {
		return getTargetDocument().getFloat(path, def);
	}

	@Override
	default double getDouble(@Nonnull String path) {
		return getTargetDocument().getDouble(path);
	}

	@Override
	default double getDouble(@Nonnull String path, double def) {
		return getTargetDocument().getDouble(path, def);
	}

	@Override
	default UUID getUniqueId(@Nonnull String path) {
		return getTargetDocument().getUniqueId(path);
	}

	@Override
	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull Class<E> enumClass) {
		return getTargetDocument().getEnum(path, enumClass);
	}

	@Override
	default <E extends Enum<?>> E getEnum(@Nonnull String path, @Nonnull E def) {
		return getTargetDocument().getEnum(path, def);
	}

	@Override
	default <T> T getInstance(@Nonnull String path, @Nonnull Class<T> classOfT) {
		return getTargetDocument().getInstance(path, classOfT);
	}

	@Nonnull
	@Override
	default List<Document> getDocuments(@Nonnull String path) {
		return getTargetDocument().getDocuments(path);
	}

	@Nonnull
	@Override
	default List<Bundle> getBundles(@Nonnull String path) {
		return getTargetDocument().getBundles(path);
	}

	@Nonnull
	@Override
	default List<String> getStrings(@Nonnull String path) {
		return getTargetDocument().getStrings(path);
	}

	@Nonnull
	@Override
	default List<Boolean> getBooleans(@Nonnull String path) {
		return getTargetDocument().getBooleans(path);
	}

	@Nonnull
	@Override
	default List<Long> getLongs(@Nonnull String path) {
		return getTargetDocument().getLongs(path);
	}

	@Nonnull
	@Override
	default List<Integer> getInts(@Nonnull String path) {
		return getTargetDocument().getInts(path);
	}

	@Nonnull
	@Override
	default List<Short> getShorts(@Nonnull String path) {
		return getTargetDocument().getShorts(path);
	}

	@Nonnull
	@Override
	default List<Byte> getBytes(@Nonnull String path) {
		return getTargetDocument().getBytes(path);
	}

	@Nonnull
	@Override
	default List<Float> getFloats(@Nonnull String path) {
		return getTargetDocument().getFloats(path);
	}

	@Override
	default List<Double> getDoubles(@Nonnull String path) {
		return getTargetDocument().getDoubles(path);
	}

	@Override
	default List<UUID> getUniqueIds(@Nonnull String path) {
		return getTargetDocument().getUniqueIds(path);
	}

	@Override
	default <E extends Enum<?>> List<E> getEnums(@Nonnull String path, @Nonnull Class<E> enumClass) {
		return getTargetDocument().getEnums(path, enumClass);
	}

	@Override
	default <T> List<T> getInstances(@Nonnull String path, @Nonnull Class<T> classOfT) {
		return getTargetDocument().getInstances(path, classOfT);
	}

	@Override
	default boolean canEdit() {
		return getTargetDocument().canEdit();
	}

	@Nonnull
	@Override
	default Document markUneditable() {
		return getTargetDocument().markUneditable();
	}

	@Nonnull
	@Override
	default Document referenceUneditable() {
		return getTargetDocument().referenceUneditable();
	}

	@Nonnull
	@Override
	default Document set(@Nonnull String path, @Nullable Object value) {
		return getTargetDocument().set(path, value);
	}

	@Nonnull
	@Override
	default Document set(@Nonnull Object values) {
		return getTargetDocument().set(values);
	}

	@Nonnull
	@Override
	default Document remove(@Nonnull String path) {
		return getTargetDocument().remove(path);
	}

	@Nonnull
	@Override
	default Document clear() {
		return getTargetDocument().clear();
	}

	@Override
	default void forEach(@Nonnull BiConsumer<? super String, ? super Object> action) {
		getTargetDocument().forEach(action);
	}

	@Override
	default void forEachEntry(@Nonnull BiConsumer<? super String, ? super IEntry> action) {
		getTargetDocument().forEachEntry(action);
	}

	@Override
	default void write(@Nonnull Writer writer) {
		getTargetDocument().write(writer);
	}

	@Override
	default void saveToFile(@Nonnull Path file) throws IOException {
		getTargetDocument().saveToFile(file);
	}

	@Override
	default void saveToFile(@Nonnull File file) throws IOException {
		getTargetDocument().saveToFile(file);
	}

	@Nonnull
	@Override
	default Document apply(@Nonnull Consumer<? super Document> action) {
		return getTargetDocument().apply(action);
	}

	@Nonnull
	@Override
	default Document applyIf(boolean condition, @Nonnull Consumer<? super Document> action) {
		return getTargetDocument().applyIf(condition, action);
	}
}
