package cloud.hytora.common.collection.pair;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

/**
 * Implementation of the {@link ValueHolder} for holding <b>three</b> values
 *
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 * @param <T> The type of the third value
 *
 * @author Lystx
 * @since SNAPSHOT-1.1
 */
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class Triple<F, S, T> implements ValueHolder {


	@Nonnull
	public static <F, S, T> Triple<F, S, T> ofFirst(@Nullable F first) {
		return of(first, null, null);
	}

	@Nonnull
	public static <F, S, T> Triple<F, S, T> ofSecond(@Nullable S second) {
		return of(null, second, null);
	}

	@Nonnull
	public static <F, S, T> Triple<F, S, T> ofThird(@Nullable T third) {
		return of(null, null, third);
	}

	@Nonnull
	public static <F, S, T> Triple<F, S, T> of(@Nullable F first, @Nullable S second, @Nullable T third) {
		return new Triple<>(first, second, third);
	}

	@Nonnull
	public static <F, S, T> Triple<F, S, T> empty() {
		return new Triple<>();
	}

	@Nonnull
	@CheckReturnValue
	public <ToF, ToS, ToT> Triple<ToF, ToS, ToT> map(@Nonnull Function<? super F, ? extends ToF> firstMapper,
													 @Nonnull Function<? super S, ? extends ToS> secondMapper,
													 @Nonnull Function<? super T, ? extends ToT> thirdMapper) {
		return of(firstMapper.apply(first), secondMapper.apply(second), thirdMapper.apply(third));
	}


	/**
	 * The first value stored
	 */
	protected F first;

	/**
	 * The second value stored
	 */
	protected S second;

	/**
	 * The third value stored
	 */
	protected T third;

	@Override
	public final int amount() {
		return 3;
	}

	@Nonnull
	@Override
	public final Object[] values() {
		return new Object[] { first, second, third };
	}

	@Override
	public boolean noneNull() {
		return first != null && second != null && third != null;
	}

	@Override
	public boolean allNull() {
		return first == null && second == null && third == null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Triple<?, ?, ?> triple = (Triple<?, ?, ?>) o;
		return Objects.equals(first, triple.first) && Objects.equals(second, triple.second) && Objects.equals(third, triple.third);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, third);
	}

	@Override
	public String toString() {
		return "Triple[" + first + ", " + second + ", " + third + "]";
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public <E> E get(int index) {
		return index == 1 ? (E) first : (index == 2 ? (E) second : (E) third);
	}
}
