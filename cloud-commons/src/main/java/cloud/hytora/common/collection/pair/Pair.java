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
 * Implementation of the {@link ValueHolder} for holding <b>two</b> values
 *
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 *
 * @author Lystx
 * @since SNAPSHOT-1.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Pair<F, S> implements ValueHolder {

	@Nonnull
	@CheckReturnValue
	public <ToF, ToS> Pair<ToF, ToS> map(@Nonnull Function<? super F, ? extends ToF> firstMapper,
										 @Nonnull Function<? super S, ? extends ToS> secondMapper) {
		return of(firstMapper.apply(first), secondMapper.apply(second));
	}

	@Nonnull
	public static <F, S> Pair<F, S> ofFirst(@Nullable F frist) {
		return new Pair<>(frist, null);
	}

	@Nonnull
	public static <F, S> Pair<F, S> ofSecond(@Nullable S second) {
		return new Pair<>(null, second);
	}

	@Nonnull
	public static <F, S> Pair<F, S> of(@Nullable F first, @Nullable S second) {
		return new Pair<>(first, second);
	}

	@Nonnull
	public static <F, S> Pair<F, S> empty() {
		return new Pair<>();
	}

	/**
	 * The first value stored
	 */
	protected F first;

	/**
	 * The second value stored
	 */
	protected S second;

	@Override
	public final int amount() {
		return 2;
	}

	@Nonnull
	@Override
	public final Object[] values() {
		return new Object[] { first, second };
	}

	@Override
	public boolean noneNull() {
		return first != null && second != null;
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public <T> T get(int index) {
		return index == 1 ? (T) first : (T) second;
	}

	@Override
	public boolean allNull() {
		return first == null && second == null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Pair<?, ?> pair = (Pair<?, ?>) o;
		return Objects.equals(first, pair.first) && Objects.equals(second, pair.second);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second);
	}

	@Override
	public String toString() {
		return "Tuple[" + first + ", " + second + "]";
	}

}
