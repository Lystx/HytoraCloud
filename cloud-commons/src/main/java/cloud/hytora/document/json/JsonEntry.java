package cloud.hytora.document.json;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import cloud.hytora.simplejson.api.enums.JsonFormat;
import cloud.hytora.simplejson.elements.JsonEntity;
import cloud.hytora.simplejson.elements.JsonLiteral;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;


public class JsonEntry implements IEntry {

	protected final JsonEntity entity;

	public JsonEntry(@Nullable Object value) {
		this(JsonHelper.DEFAULT_JSON.toJson(value));
	}

	public JsonEntry(@Nullable JsonEntity element) {
		this.entity = element == null ? JsonLiteral.NULL : element;
	}

	@Override
	public boolean isNull() {
		return entity.isNull();
	}

	@Override
	public boolean isBundle() {
		return entity.isArray();
	}

	@Override
	public boolean isDocument() {
		return entity.isJsonObject();
	}

	@Override
	public boolean isNumber() {
		return entity.isPrimitive() && entity.isNumber();
	}

	@Override
	public boolean isBoolean() {
		return entity.isPrimitive() && entity.isBoolean();
	}

	@Override
	public boolean isChar() {
		return entity.isPrimitive() && entity.isString() && entity.asString().length() == 1;
	}

	@Override
	public Object toObject() {
		return this.isNull() ? null
				: this.isDocument() ? this.toDocument()
				: this.isBundle() ? this.toBundle()
				: this.isBoolean() ? this.toBoolean()
				: this.isNumber() ? this.toNumber()
				: this.isChar() ? this.toChar()
				: entity;
	}

	@Override
	public String toString(@Nullable String def) {
		return isNull() ? def : entity.isPrimitive() && entity.isString() ? entity.asString() : entity.toString();
	}

	@Override
	public String toString() {
		return toString(null);
	}


	@Override
	public long toLong(long def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asLong();
	}

	@Override
	public int toInt(int def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asInt();
	}

	@Override
	public short toShort(short def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asShort();
	}

	@Override
	public byte toByte(byte def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asByte();
	}

	@Override
	public float toFloat(float def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asFloat();
	}

	@Override
	public double toDouble(double def) {
		if (isNull()) return def;
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asDouble();
	}

	@Override
	public char toChar(char def) {
		if (isNull()) return def;
		if (!isChar()) throw new IllegalStateException("Not a char");
		return entity.asString().toCharArray()[0];
	}

	@Override
	public boolean toBoolean(boolean def) {
		if (isNull()) return def;
		if (!isBoolean()) throw new IllegalStateException("Not a number");
		return entity.asBoolean();
	}

	@Override
	public Number toNumber() {
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return entity.asNumber();
	}

	@Override
	public Document toDocument() {
		if (!isDocument()) throw new IllegalStateException("Not a document");
		return new JsonDocument(entity.asJsonObject());
	}

	@Override
	public Bundle toBundle() {
		if (isNull()) throw new IllegalStateException("Not a bundle");
		if (entity.isArray()) return new JsonBundle(entity.asJsonArray());
		return Bundle.newJsonBundle(entity);
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return JsonHelper.DEFAULT_JSON.fromJson(entity, classOfT);
	}

	@Override
	public <T> T toInstance(@NotNull Type typeOf) {
		throw new UnsupportedOperationException("Not usable in Vson");
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(entity).toString(JsonFormat.RAW);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return JsonHelper.DEFAULT_JSON.toJson(entity).toString(JsonFormat.SIMPLE);
	}
}
