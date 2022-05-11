package cloud.hytora.document.empty;

import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.document.IEntry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;


public class EmptyEntry implements IEntry {

	public static final EmptyEntry INSTANCE = new EmptyEntry();

	@Override
	public boolean isNull() {
		return true;
	}

	@Override
	public boolean isBundle() {
		return false;
	}

	@Override
	public boolean isDocument() {
		return false;
	}

	@Override
	public boolean isObject() {
		return false;
	}

	@Override
	public boolean isNumber() {
		return false;
	}

	@Override
	public boolean isBoolean() {
		return false;
	}

	@Override
	public boolean isChar() {
		return false;
	}

	@Override
	public Object toObject() {
		return null;
	}

	@Override
	public String toString() {
		return null;
	}

	@Override
	public String toString(@Nullable String def) {
		return def;
	}

	@Override
	public long toLong(long def) {
		return def;
	}

	@Override
	public int toInt(int def) {
		return def;
	}

	@Override
	public short toShort(short def) {
		return def;
	}

	@Override
	public byte toByte(byte def) {
		return def;
	}

	@Override
	public float toFloat(float def) {
		return def;
	}

	@Override
	public double toDouble(double def) {
		return def;
	}

	@Override
	public char toChar(char def) {
		return def;
	}

	@Override
	public boolean toBoolean(boolean def) {
		return def;
	}

	@Override
	public Number toNumber() {
		return null;
	}

	@Override
	public Document toDocument() {
		return null;
	}

	@Override
	public Bundle toBundle() {
		return null;
	}

	@Override
	public <T> T toInstance(@Nonnull Class<T> classOfT) {
		return null;
	}

	@Override
	public <T> T toInstance(@NotNull Type typeOf) {
		return null;
	}

	@Nonnull
	@Override
	public String asRawJsonString() {
		return "null";
	}

	@Nonnull
	@Override
	public String asFormattedJsonString() {
		return "null";
	}
}
