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
 * Implementation of the {@link ValueHolder} for holding <b>four</b> values
 *
 * @param <F> The type of the first value
 * @param <S> The type of the second value
 * @param <T> The type of the third value
 * @param <FF> The type of the fourth value
 *
 * @author Lystx
 * @since SNAPSHOT-1.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Quadro<F, S, T, FF> implements ValueHolder {

	@Nonnull
	@CheckReturnValue
	public <ToF, ToS, ToT, ToFF> Quadro<ToF, ToS, ToT, ToFF> map(@Nonnull Function<? super F, ? extends ToF> firstMapper,
																 @Nonnull Function<? super S, ? extends ToS> secondMapper,
																 @Nonnull Function<? super T, ? extends ToT> thirdMapper,
																 @Nonnull Function<? super FF, ? extends ToFF> fourthMapper) {
		return of(firstMapper.apply(first), secondMapper.apply(second), thirdMapper.apply(third), fourthMapper.apply(fourth));
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> ofFirst(@Nullable F first) {
		return of(first, null, null, null);
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> ofSecond(@Nullable S second) {
		return of(null, second, null, null);
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> ofThird(@Nullable T third) {
		return of(null, null, third, null);
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> ofFourth(@Nullable FF fourth) {
		return of(null, null, null, fourth);
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> of(@Nullable F first, @Nullable S second, @Nullable T third, @Nullable FF fourth) {
		return new Quadro<>(first, second, third, fourth);
	}

	@Nonnull
	public static <F, S, T, FF> Quadro<F, S, T, FF> empty() {
		return new Quadro<>();
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

	/**
	 * The fourth value stored
	 */
	protected FF fourth;

	@Override
	public final int amount() {
		return 4;
	}

	@Nonnull
	@Override
	public final Object[] values() {
		return new Object[] { first, second, third, first };
	}

	@Override
	public boolean noneNull() {
		return first != null && second != null && third != null && fourth != null;
	}

	@Override
	public boolean allNull() {
		return first == null && second == null && third == null && fourth != null;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Quadro<?, ?, ?, ?> quadro = (Quadro<?, ?, ?, ?>) o;
		return Objects.equals(first, quadro.first) && Objects.equals(second, quadro.second) && Objects.equals(third, quadro.third) && Objects.equals(fourth, quadro.fourth);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first, second, third, fourth);
	}

	@Override
	public String toString() {
		return "Quadro[" + first + ", " + second + ", " + third + ", " + fourth + "]";
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public <E> E get(int index) {
		return index == 1 ? (E) first : (index == 2 ? (E) second : (index == 3 ? (E) third : (E) fourth));
	}
}
