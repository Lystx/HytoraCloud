package cloud.hytora.common.function;

import cloud.hytora.common.collection.WrappedException;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

/**

 * @since 1.2.2
 */
@FunctionalInterface
public interface ExceptionallyRunnable extends Runnable, Callable<Void> {

	@Override
	default void run() {
		this.run(WrappedException::rethrow);
	}


	default void run(Consumer<Throwable> exceptionCatcher) {
		try {
			runExceptionally();
		} catch (Exception ex) {
			exceptionCatcher.accept(ex);
		}
	}

	@Override
	default Void call() throws Exception {
		runExceptionally();
		return null;
	}

	void runExceptionally() throws Exception;

}
