package cloud.hytora.document.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;


public class GsonEntry implements IEntry {

	protected final JsonElement element;

	public GsonEntry(@Nullable Object value) {
		this(GsonHelper.DEFAULT_GSON.toJsonTree(value));
	}

	public GsonEntry(@Nullable JsonElement element) {
		this.element = element == null ? JsonNull.INSTANCE : element;
	}

	@Override
	public boolean isNull() {
		return element.isJsonNull();
	}

	@Override
	public boolean isBundle() {
		return element.isJsonArray();
	}

	@Override
	public boolean isDocument() {
		return element.isJsonObject();
	}

	@Override
	public boolean isNumber() {
		return element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber();
	}

	@Override
	public boolean isBoolean() {
		return element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean();
	}

	@Override
	public boolean isChar() {
		return element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() && element.getAsJsonPrimitive().getAsString().length() == 1;
	}

	@Override
	public Object toObject() {
		return this.isNull() ? null
				: this.isDocument() ? this.toDocument()
				: this.isBundle() ? this.toBundle()
				: this.isBoolean() ? this.toBoolean()
				: this.isNumber() ? this.toNumber()
				: this.isChar() ? this.toChar()
				: element;
	}

	@Override
	public String toString(@Nullable String def) {
		return isNull() ? def : element.isJsonPrimitive() && element.getAsJsonPrimitive().isString() ? element.getAsString() : element.toString();
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public long toLong(long def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsLong();
	}

	@Override
	public int toInt(int def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsInt();
	}

	@Override
	public short toShort(short def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsShort();
	}

	@Override
	public byte toByte(byte def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsByte();
	}

	@Override
	public float toFloat(float def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsFloat();
	}

	@Override
	public double toDouble(double def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsDouble();
	}

	@Override
	public char toChar(char def) {
		if (isNull()) return def;
		if (!isChar()) throw new IllegalStateException("Not a char");
		return element.getAsJsonPrimitive().getAsCharacter();
	}

	@Override
	public boolean toBoolean(boolean def) {
		if (isNull()) return def;
		if (!isBoolean()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsBoolean();
	}

	@Override
	public Number toNumber() {
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return element.getAsJsonPrimitive().getAsNumber();
	}

	@Override
	public Document toDocument() {
		if (!isDocument()) throw new IllegalStateException("Not a document");
		return new GsonDocument(element.getAsJsonObject());
	}

	@Override
	public Bundle toBundle() {
		if (isNull()) throw new IllegalStateException("Not a bundle");
		if (element.isJsonArray()) return new GsonBundle(element.getAsJsonArray());
		return DocumentFactory.newJsonBundle(element);
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return GsonHelper.DEFAULT_GSON.fromJson(element, classOfT);
	}

	@Override
	public <T> T toInstance(@NotNull Type typeOf) {
		return GsonHelper.DEFAULT_GSON.fromJson(element, typeOf);
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return GsonHelper.DEFAULT_GSON.toJson(element);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return GsonHelper.PRETTY_GSON.toJson(element);
	}
}
