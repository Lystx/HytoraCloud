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
 * Implementation of the {@link ValueHolder} for holding <b>one</b> values
 *
 * @param <F> The type of the value
 *
 * @author Lystx
 * @since SNAPSHOT-1.1
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Wrap<F> implements ValueHolder {

	@Nonnull
	public <T> Wrap<T> map(@Nonnull Function<? super F, ? extends T> mapper) {
		return new Wrap<>(mapper.apply(first));
	}

	@Nonnull
	@CheckReturnValue
	public static <F> Wrap<F> of(@Nullable F first) {
		return new Wrap<>(first);
	}

	@Nonnull
	@CheckReturnValue
	public static <F> Wrap<F> empty() {
		return new Wrap<>();
	}

	/**
	 * The wrapped value
	 */
	protected F first;

	@Override
	public final int amount() {
		return 1;
	}

	@Nonnull
	@Override
	public final Object[] values() {
		return new Object[] { first };
	}

	@Override
	public boolean allNull() {
		return first == null;
	}

	@Override
	public boolean noneNull() {
		return first != null;
	}

	@Override
	public String toString() {
		return "Wrap[" + first + "]";
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Wrap<?> wrap = (Wrap<?>) o;
		return Objects.equals(first, wrap.first);
	}

	@Override
	public int hashCode() {
		return Objects.hash(first);
	}

	@org.jetbrains.annotations.Nullable
	@Override
	public <T> T get(int index) {
		return index == 1 ? (T) first : null;
	}
}
