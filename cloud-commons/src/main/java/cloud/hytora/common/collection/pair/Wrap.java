package cloud.hytora.common.collection.pair;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;


public class Wrap<F> implements Pair {

	protected F first;

	public Wrap() {
	}

	public Wrap(F first) {
		this.first = first;
	}

	public F getFirst() {
		return first;
	}

	public void setFirst(F first) {
		this.first = first;
	}

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

	@Nonnull
	public <T> Wrap<T> map(@Nonnull Function<? super F, ? extends T> mapper) {
		return new Wrap<>(mapper.apply(first));
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
}
