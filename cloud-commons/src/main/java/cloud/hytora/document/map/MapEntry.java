
package cloud.hytora.document.map;

import cloud.hytora.document.gson.GsonHelper;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.IEntry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;


public class MapEntry implements IEntry {

	private Object value;

	public MapEntry(@Nullable Object value) {
		this.value = value;
	}

	@Override
	public boolean isNull() {
		return value == null;
	}

	@Override
	public boolean isBundle() {
		return value instanceof Bundle || value instanceof Collection || value instanceof Iterable || value instanceof Iterator || value.getClass().isArray();
	}

	@Override
	public boolean isDocument() {
		return value instanceof Document || value instanceof Map;
	}

	@Override
	public boolean isNumber() {
		return value instanceof Number;
	}

	@Override
	public boolean isBoolean() {
		return value instanceof Boolean;
	}

	@Override
	public boolean isChar() {
		return value instanceof Character || value instanceof String && ((String)value).length() == 1;
	}

	@Override
	public Object toObject() {
		return value;
	}

	@Override
	public String toString() {
		return toString(null);
	}

	@Override
	public String toString(@Nullable String def) {
		return value == null ? def : String.valueOf(value);
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
		return value instanceof Character ? (char) value : String.valueOf(value).charAt(0);
	}

	@Override
	public boolean toBoolean(boolean def) {
		return isNull() ? def : isBoolean() ? (boolean) value : isNumber() ? toInt() == 1 : Boolean.parseBoolean(toString());
	}

	@Override
	public Number toNumber() {
		if (!isNumber()) throw new IllegalStateException("Not a number");
		return (Number) value;
	}

	@Override
	public Document toDocument() {
		if (!isDocument()) throw new IllegalStateException("Not a document");
		return (Document) value;
	}

	@Override
	public Bundle toBundle() {
		if (isBundle()) throw new IllegalStateException("Not a bundle");
		if (value instanceof Collection) value = new MapBundle((Collection<?>) value);
		if (value instanceof Bundle) return (Bundle) value;
		return (Bundle) (value = DocumentFactory.newJsonBundle(value));
	}

	@Override
	public UUID toUniqueId() {
		if (value == null) return null;
		if (value instanceof UUID) return (UUID) value;
		if (value instanceof String) return UUID.fromString((String) value);
		throw new IllegalStateException("Not a uuid");
	}

	@Override
	public OffsetDateTime toOffsetDateTime() {
		if (value == null) return null;
		if (value instanceof OffsetDateTime) return (OffsetDateTime) value;
		if (value instanceof String) return OffsetDateTime.parse((CharSequence) value);
		if (value instanceof Date) return ((Date)value).toInstant().atOffset(ZoneOffset.UTC);
		if (value instanceof Instant) return ((Instant)value).atOffset(ZoneOffset.UTC);
		throw new IllegalStateException("Not a OffsetDateTime");
	}

	@Override
	public Date toDate() {
		if (value == null) return null;
		if (value instanceof Date) return (Date) value;
		if (value instanceof String) {
			try {
				return DateFormat.getDateTimeInstance().parse((String) value);
			} catch (ParseException ex) {
				throw new IllegalStateException("Not a date");
			}
		}
		if (value instanceof OffsetDateTime) return Date.from(((OffsetDateTime)value).toInstant());
		if (value instanceof Instant) return Date.from((Instant) value);
		throw new IllegalStateException("Not a date");
	}

	@Override
	public Color toColor() {
		if (value == null) return null;
		if (value instanceof Color) return (Color) value;
		if (value instanceof String) return Color.decode((String) value);
		throw new IllegalStateException("Not a color");
	}

	@Override
	public Class<?> toClass() {
		if (value == null) return null;
		if (value instanceof Class) return (Class<?>) value;
		if (value instanceof String) {
			try {
				return Class.forName((String) value);
			} catch (ClassNotFoundException ex) {
				throw new IllegalStateException("Not a class");
			}
		}
		throw new IllegalStateException("Not a class");
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		if (classOfT.isInstance(value)) {
			return classOfT.cast(value);
		}
		return DocumentFactory.newJsonEntry(value).toInstance(classOfT);
	}

	@Override
	public <T> T toInstance(@NotNull Type typeOf) {
		return DocumentFactory.newJsonEntry(value).toInstance(typeOf);
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return GsonHelper.toGsonEntry(value).asRawJsonString();
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return GsonHelper.toGsonEntry(value).asFormattedJsonString();
	}
}
