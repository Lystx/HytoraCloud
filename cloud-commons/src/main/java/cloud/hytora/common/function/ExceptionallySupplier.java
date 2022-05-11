package cloud.hytora.common.function;

import cloud.hytora.common.collection.WrappedException;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

/**

 * @since 1.2.2
 */
@FunctionalInterface
public interface ExceptionallySupplier<T> extends Supplier<T>, Callable<T> {

	@Override
	default T get() {
		try {
			return getExceptionally();
		} catch (Exception ex) {
			throw WrappedException.rethrow(ex);
		}
	}

	@Override
	default T call() throws Exception {
		return getExceptionally();
	}

	T getExceptionally() throws Exception;

}
