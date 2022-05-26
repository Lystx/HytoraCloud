package cloud.hytora.document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.awt.*;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

/**
 * A {@link IEntry} represents some property snapshot of a {@link Document} or {@link Bundle}
 * which can be a string, number, document, bundle, ...
 *
 * A {@link IEntry} is immutable, when the value is changed in the {@link Document} or {@link Bundle},
 * this {@link IEntry} will not be effected.
 *
 * @see Document#getEntry(String)
 * @see Bundle#getEntry(int)
 *
 * @see Document#entries()
 * @see Bundle#entries()
 *
 */
public interface IEntry extends JsonEntity {

	/**
	 * @return whether this entry is {@code null}, non-primitive getters will return {@code null}
	 */
	boolean isNull();

	/**
	 * @return whether this entry is an array/collection/bundle
	 */
	boolean isBundle();

	/**
	 * @return whether this entry is a document
	 */
	boolean isDocument();


	default boolean isPrimitive() {
		return isBoolean() || isNumber() || isChar();
	}

	/**
	 * @return whether this entry is an object (number, string, boolean, ...), not a document or bundle
	 */
	default boolean isObject() {
		return !isNull() && !isBundle() && !isDocument();
	}

	/**
	 * @return whether this entry is a number
	 */
	boolean isNumber();

	/**
	 * @return whether this entry is a boolean
	 */
	boolean isBoolean();

	/**
	 * @return whether this entry is a char
	 */
	boolean isChar();

	/**
	 * @return this entry as unpacked object, could be {@code null} if this is {@link #isNull() null}
	 */
	Object toObject();

	/**
	 * Default implementation {@code toString(null)}
	 *
	 * @return this entry as string, could be {@code null} if this is {@link #isNull() null}
	 */
	String toString();

	String toString(@Nullable String def);

	@Nonnull
	@Override
	String asRawJsonString();

	@Nonnull
	@Override
	String asFormattedJsonString();

	/**
	 * @return this entry as long, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a long
	 */
	long toLong(long def);

	default long toLong() {
		return toLong(0);
	}

	/**
	 * @return this entry as int, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to an int
	 */
	int toInt(int def);

	default int toInt() {
		return toInt(0);
	}

	/**
	 * @return this entry as short, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a short
	 */
	short toShort(short def);

	default short toShort() {
		return toShort((short) 0);
	}

	/**
	 * @return this entry as byte, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a byte
	 */
	byte toByte(byte def);

	default byte toByte() {
		return toByte((byte) 0);
	}

	/**
	 * @return this entry as float, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a float
	 */
	float toFloat(float def);

	default float toFloat() {
		return toFloat(0);
	}

	/**
	 * @return this entry as double, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a double
	 */
	double toDouble(double def);

	default double toDouble() {
		return toDouble(0);
	}

	/**
	 * @return this entry as char, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a char
	 */
	char toChar(char def);

	default char toChar() {
		return toChar((char) 0);
	}

	/**
	 * @return this entry as boolean, {@code 1} and {@code true} will return {@code true}, or {@code def} if this is {@link #isNull() null}
	 */
	boolean toBoolean(boolean def);

	default boolean toBoolean() {
		return toBoolean(false);
	}

	/**
	 * @return this entry as number
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a number
	 */
	Number toNumber();

	/**
	 * @return this entry as {@link Document}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Document}
	 */
	Document toDocument();

	/**
	 * @return this entry as {@link Bundle}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Bundle}
	 */
	Bundle toBundle();

	/**
	 * @return this entry as {@link UUID}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to an {@link UUID}
	 */
	default UUID toUniqueId() {
		return toInstance(UUID.class);
	}

	/**
	 * @return this entry as {@link OffsetDateTime}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to an {@link OffsetDateTime}
	 */
	default OffsetDateTime toOffsetDateTime() {
		return toInstance(OffsetDateTime.class);
	}

	/**
	 * @return this entry as {@link Date}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Date}
	 */
	default Date toDate() {
		return toInstance(Date.class);
	}

	/**
	 * @return this entry as {@link Color}, could be {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Color}
	 */
	default Color toColor() {
		return toInstance(Color.class);
	}

	/**
	 * @return this entry as {@link Class}, or {@code null} if the class is not found, or this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link String}
	 */
	default Class<?> toClass() {
		return toInstance(Class.class);
	}

	/**
	 * @return this entry as {@link Enum EnumConstant} of the given class, or {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Enum EnumConstant} of the given class
	 */
	default <E extends Enum<?>> E toEnum(@Nonnull Class<E> enumClass) {
		return toInstance(enumClass);
	}

	/**
	 * @return this entry as {@link Enum EnumConstant} of the given class, or {@code def} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If this entry cannot be converted to a {@link Enum EnumConstant} of the given class
	 */
	@SuppressWarnings("unchecked")
	default <E extends Enum<?>> E toEnum(@Nonnull E def) {
		E value = toEnum((Class<E>) def.getClass());
		return value == null ? def : value;
	}

	/**
	 * @param classOfT the class this entry should be converted to
	 * @return this entry converted to the given class, or {@code null} if this is {@link #isNull() null}
	 *
	 * @throws IllegalStateException
	 *         If the value cannot be converted to the given instance class by the underlying library
	 */
	<T> T toInstance(@Nonnull Class<T> classOfT);

	<T> T toInstance(@Nonnull Type typeOf);

	default <T> T toInstance(@Nonnull Class<T> classOfT, @Nullable T def) {
		T value = toInstance(classOfT);
		return value == null ? def : value;
	}

}
