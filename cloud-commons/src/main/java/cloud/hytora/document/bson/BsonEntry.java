package cloud.hytora.document.bson;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import org.bson.BsonNull;
import org.bson.BsonValue;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;


public class BsonEntry implements IEntry {

	protected Object value;
	protected BsonValue bsonValue;

	public BsonEntry(@Nullable Object value) {
		this.bsonValue = value instanceof BsonValue ? (BsonValue) value : value == null ? BsonNull.VALUE : null;
		this.value = value;
	}

	@Override
	public boolean isNull() {
		return value == null;
	}

	@Override
	public boolean isBundle() {
		return bsonValue != null && bsonValue.isArray();
	}

	@Override
	public boolean isDocument() {
		return value instanceof org.bson.Document || value instanceof org.bson.BsonDocument;
	}


	@Override
	public boolean isNumber() {
		return value instanceof Number || bsonValue != null && bsonValue.isNumber();
	}

	@Override
	public boolean isBoolean() {
		return value instanceof Boolean || bsonValue != null && bsonValue.isBoolean();
	}

	@Override
	public boolean isChar() {
		return value instanceof String && ((String)value).length() == 1
			|| value instanceof Character
			|| bsonValue != null && bsonValue.isString() && bsonValue.asString().getValue().length() == 1;
	}

	@Override
	public Object toObject() {
		return this.isNull() ? null
				: this.isDocument() ? this.toDocument()
				: this.isBundle() ? this.toBundle()
				: this.isBoolean() ? this.toBoolean()
				: this.isNumber() ? this.toNumber()
				: this.isChar() ? this.toChar()
				: value;
	}

	@Override
	public String toString(@Nullable String def) {
		return isNull() ? def :
			bsonValue == null ? value.toString() :
			bsonValue.isString() ? bsonValue.asString().getValue() :
			bsonValue.isSymbol() ? bsonValue.asSymbol().getSymbol() :
			asRawJsonString();
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public long toLong(long def) {
		return isNull() ? def : toNumber().longValue();
	}

	@Override
	public int toInt(int def) {
		return isNull() ? def : toNumber().intValue();
	}

	@Override
	public short toShort(short def) {
		return isNull() ? def : toNumber().shortValue();
	}

	@Override
	public byte toByte(byte def) {
		return isNull() ? def : toNumber().byteValue();
	}

	@Override
	public float toFloat(float def) {
		return isNull() ? def : toNumber().floatValue();
	}

	@Override
	public double toDouble(double def) {
		return isNull() ? def : toNumber().doubleValue();
	}

	@Override
	public char toChar(char def) {
		if (isNull()) return def;
		if (!isChar()) throw new IllegalStateException("Not a char");
		return toString().charAt(0);
	}

	@Override
	public boolean toBoolean(boolean def) {
		return isNull() ? def :
			value instanceof Boolean ? (boolean) value :
			value instanceof String ? Boolean.parseBoolean((String) value) :
			bsonValue == null ? false :
			bsonValue.isBoolean() ? bsonValue.asBoolean().getValue() :
			bsonValue.isString() ? Boolean.parseBoolean(bsonValue.asString().getValue())
			: def;
	}

	@Override
	public Number toNumber() {
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return value instanceof Number ? (Number) value :
			bsonValue.isInt32() ? bsonValue.asInt32().getValue() :
			bsonValue.isInt64() ? bsonValue.asInt64().getValue() :
			bsonValue.isDecimal128() ? bsonValue.asDecimal128().getValue() :
			bsonValue.isDouble() ? bsonValue.asDouble().getValue() :
			bsonValue.asNumber().doubleValue();
	}

	@Override
	public Document toDocument() {
		if (!isDocument()) throw new IllegalStateException("Not a Document");
		return new BsonDocument(value instanceof org.bson.Document ? (org.bson.Document) value : org.bson.Document.parse(bsonValue.asDocument().toJson()), new AtomicBoolean(false));
	}

	@Override
	public Bundle toBundle() {
		if (!isBundle()) throw new IllegalStateException("Not a bundle");
		return new BsonBundle(bsonValue.asArray(), new AtomicBoolean(false));
	}

	@Override
	public UUID toUniqueId() {
		if (isNull()) return null;
		if (value instanceof UUID) return (UUID) value;
		if (value instanceof String) return UUID.fromString(String.valueOf(value));
		throw new IllegalStateException("Not a uuid");
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		if (classOfT.isInstance(value))
			return classOfT.cast(value);

		return DocumentFactory.newJsonEntry(value).toInstance(classOfT);
	}

	@Override
	public <T> T toInstance(@NotNull Type typeOf) {
		return DocumentFactory.newJsonEntry(value).toInstance(typeOf);
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return BsonHelper.toJson(value, false);
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return BsonHelper.toJson(value, true);
	}

}
