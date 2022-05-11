package cloud.hytora.common.function;

import cloud.hytora.common.collection.WrappedException;

import java.util.function.BiFunction;

/**

 * @since 1.2.4
 */
@FunctionalInterface
public interface ExceptionallyBiFunction<T, U, R> extends BiFunction<T, U, R> {

	@Override
	default R apply(T t, U u) {
		try {
			return applyExceptionally(t, u);
		} catch (Exception ex) {
			throw WrappedException.rethrow(ex);
		}
	}

	R applyExceptionally(T t, U u) throws Exception;

}
