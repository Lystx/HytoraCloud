package cloud.hytora.common.collection.pair;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Interface for value holder implementations that hold a certain amount of values
 *
 * @see Wrap
 * @see Pair
 * @see Triple
 * @see Quadro
 *
 * @author Lystx
 * @since SNAPSHOT-1.0
 */
public interface ValueHolder {

	/**
	 * @return The amount of values
	 */
	@Nonnegative
	int amount();

	/**
	 * @return an array of raw objects that are in this pair instance
	 */
	@Nonnull
	Object[] values();

	/**
	 * @return {@code true} when all of the values are null, {@code false} otherwise
	 */
	boolean allNull();

	/**
	 * @return {@code true} when none of the values are null, {@code false} otherwise
	 */
	boolean noneNull();

	/**
	 * Returns the value at the given index of this object
	 *
	 * @param index the index
	 * @param <T> the generic of the object
	 * @return instance or null if not set
	 */
	@Nullable
	<T> T get(int index);
}
