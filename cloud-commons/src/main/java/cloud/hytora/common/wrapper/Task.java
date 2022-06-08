package cloud.hytora.common.wrapper;

import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.wrapper.def.SimpleTask;
import cloud.hytora.common.wrapper.exception.ValueHoldsNoObjectException;
import cloud.hytora.common.wrapper.exception.ValueImmutableException;
import cloud.hytora.common.function.ExceptionallyRunnable;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * This class represents a wrapper for an {@link Object} of your choice.
 * The object inside this wrapper might be null at any time.
 *
 * <br>
 * <br> There are multiple checks to get if there is a value being held
 * like {@link Task#isNull()} or {@link Task#isPresent()}
 *
 * <br>
 * <br>Values may be immutable, so they can't be modified after they
 * have received a value or have been updated once
 * The immutable changed can be retrieved or change using {@link Task#isImmutable()}
 * and {@link Task#setImmutable(boolean)} at any time
 */
public interface Task<T> extends Serializable {

    /**
     * The static executor for async calls
     */
    ExecutorService SERVICE = Executors.newCachedThreadPool(new NamedThreadFactory("WrapperTaskPool"));

    /**
     * Constructs a new empty {@link Task} with no object being held
     * The created value is not immutable, so it may be modified
     *
     * @param <T> the type of the object the new value should hold
     * @return the created value instance
     */
    static <T> Task<T> empty() {
        return build((T) null);
    }

    /**
     * Constructs a new empty {@link Task} with no object being held
     * The created value is not immutable, so it may be modified
     *
     * @param <T> the type of the object the new value should hold
     * @return the created value instance
     */
    static <T> Task<T> empty(Class<T> typeClass) {
        return build((T) null);
    }

    static <T> Task<Boolean> multiTasking(Task<?>... tasks) {

        Task<Boolean> unitPromise = empty();
        if (tasks.length == 0) {
            return unitPromise;
        }
        for (Task<?> promise : tasks) {
            promise.addSimpleUpdateListener(o -> {
                if (Arrays.stream(tasks).allMatch(Task::isPresent) && !unitPromise.isPresent()) {
                    unitPromise.setResult(true);
                }
            });
        }
        return unitPromise;
    }

    /**
     * Constructs a new pre-filled {@link Task} with an object being held
     * The created value is not immutable, so it may be modified
     *
     * @param value the value that should be held
     * @param <T>   the type of the object the new value should hold
     * @return the created value instance
     */
    static <T> Task<T> build(T value) {
        return build(value, false);
    }

    /**
     * Constructs a new pre-filled {@link Task} with an object being held
     * The created value is not immutable, so it may be modified
     *
     * @param value the value that should be held
     * @param <T>   the type of the object the new value should hold
     * @return the created value instance
     */
    static <T> Task<T> build(Supplier<T> value) {
        return build(value.get());
    }


    /**
     * Constructs a new pre-filled {@link Task} with an object being held
     * The created value will be immutable depending on the value you provide
     * as a parameter
     *
     * @param value     the value that should be held
     * @param immutable if the value may be modified
     * @param <T>       the type of the object the new value should hold
     * @return the created value instance
     */
    static <T> Task<T> build(T value, boolean immutable) {
        return new SimpleTask<T>().setResult(value).setImmutable(immutable);
    }

    static Task<Void> runSync(@Nonnull Runnable runnable) {
        return callSync(() -> {
            runnable.run();
            return null;
        });
    }


    static Task<TaskHolder> runTaskLater(@NotNull Runnable runnable, TimeUnit unit, long delay) {
        Task<TaskHolder> task = Task.empty();
        Scheduler.runTimeScheduler().scheduleDelayedTask(() -> {
            runnable.run();
            task.setResult(TaskHolder.INSTANCE);
        }, unit.toMillis(delay));
        return task;
    }

    static Task<Void> runAsync(@Nonnull Runnable runnable) {
        return callAsync(() -> {
            runnable.run();
            return null;
        });
    }

    static Task<Void> runExceptionally(@Nonnull ExceptionallyRunnable runnable) {
        return runSync(runnable);
    }

    static Task<Void> runAsyncExceptionally(@Nonnull ExceptionallyRunnable runnable) {
        return runAsync(runnable);
    }

    static <V> Task<V> callAsync(@Nonnull Callable<V> callable) {
        Task<V> task = empty();
        task.denyNull();
        SERVICE.execute(() -> {
            try {
                task.allowNull();
                task.setResult(callable.call());
            } catch (Throwable ex) {
                task.setFailure(ex);
            }
        });
        return task;
    }

    static <V> Task<V> callSync(@Nonnull Callable<V> callable) {
        Task<V> task = empty();
        task.denyNull();
        try {
            task.allowNull();
            task.setResult(callable.call());
        } catch (Throwable ex) {
            task.setFailure(ex);
        }
        return task;
    }

    /**
     * Denies null values
     */
    Task<T> denyNull();

    /**
     * Denies null values
     */
    Task<T> allowNull();

    /**
     * Returns null if there is no provided value
     */
    T orNull();

    /**
     * Retrieves the current held object if it is set
     * This is unsafe to use because of Exception-Throwing
     * You should rather use {@link Task#orElse(Object)} to avoid exceptions
     * and provide your own custom value to return if nothing set
     * See why it throws {@link ValueHoldsNoObjectException} down below.
     * If there is no object being held right now and the value is immutable
     * the exception won't be thrown because there is no value, and maybe
     * it needs a value first to act immutable. After the value was changed
     * and it is tried to update another time it will of course throw the exception
     *
     * @return value or nothing
     * @throws ValueHoldsNoObjectException if there is no value held right now
     */
    T get() throws ValueHoldsNoObjectException;

    /**
     * Tries to get the current held object as a specific wrapper class
     * The method internally tries to cast the held object to the wrapper
     *
     * @param wrapperClass the class to get it as
     * @param <V>          the generic of the value to hold
     * @return value or null
     * @throws ValueHoldsNoObjectException if there is no value held right now
     */
    <V> V as(Class<? extends V> wrapperClass) throws ValueHoldsNoObjectException;

    /**
     * Provides an exception for this value
     *
     * @param ex the exception to set
     */
    Task<T> setFailure(Throwable ex);

    /**
     * Retrieves the {@link Throwable} that has been set
     * via {@link Task#setFailure(Throwable)}
     *
     * @return error (if set) or null
     */
    Throwable error();

    /**
     * Blocks the current thread and awaits for an update of this value
     *
     * @return current value
     */
    Task<T> syncUninterruptedly();

    default <V> V syncUninterruptedlyAndMap(Function<T, V> mapper) {
        return syncUninterruptedly().map(mapper).get();
    }

    /**
     * Sets the time-out for {@link Task#syncUninterruptedly()}
     *
     * @param unit          the unit of the timeOut
     * @param timeOut       the value for the given unit
     * @param fallbackValue the value to fall back to if timed out
     * @return current value instance
     */
    Task<T> timeOut(TimeUnit unit, int timeOut, T fallbackValue);

    default Task<T> timeOut(int timeOut) {
        return timeOut(TimeUnit.MILLISECONDS, timeOut, null);
    }

    default Task<T> timeOut(TimeUnit unit, int timeOut) {
        return timeOut(unit, timeOut, null);
    }

    /**
     * Tries to retrieve the current held object if it is set
     * If there is no current object held, the provided value
     * will be returned.
     * Unlike {@link Task#get()} this method does not throw exceptions
     *
     * @param value the optional value if no value is held
     * @return held value or provided parameter
     */
    T orElse(T value);

    /**
     * Return the value if present, otherwise invoke the parameter supplier
     * and return the result of that invocation.
     *
     * @param other the supplier to retrieve the result if nothing is held
     * @return the current held object or the value of the supplier
     */
    T orGet(Supplier<? extends T> other);

    /**
     * Tries to retrieve the current held object.
     * If there is no current object the provided {@link Throwable} will be thrown
     *
     * @param exception the exception to throw if no value is held
     * @param <E>       the type of the exception to throw
     * @return value if held or nothing (because of interrupt by exception)
     */
    <E extends Throwable> T orThrow(Supplier<? extends E> exception);

    /**
     * Tries to retrieve the current held object.
     * If there is no current object the provided {@link Throwable} will be thrown
     *
     * @param exception the exception to throw if no value is held
     * @param <E>       the type of the exception to throw
     * @return value if held or nothing (because of interrupt by exception)
     */
    <E extends Throwable> T orThrow(E exception);

    /**
     * Performs an action after testing the provided {@link Predicate}
     * If the predicate returns false the "ifFalse" {@link Runnable} will be
     * executed and if the predicate returns true the consumer "or" will be invoked
     *
     * @param predicate the check-function
     * @param ifFalse   the task to perform if the check returns false
     * @param or        the task to perform if the check returns true
     */
    void orElseDo(Predicate<T> predicate, Runnable ifFalse, Consumer<T> or);

    T getOrPerform(Predicate<Task<T>> predicate, Consumer<Task<T>> ifTrue, Consumer<Task<T>> ifFalse);

    /**
     * If a value is present, apply the provided mapping function to it,
     * and if the result is non-null, return a {@link Task} describing the
     * result. Otherwise, return an empty {@link Task}
     *
     * @param <V>    The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return a new value describing the result of applying a mapping
     */
    <V> Task<V> map(Function<T, V> mapper);

    /**
     * Maps this value thread-blocking
     *
     * @param <V>    The type of the result of the mapping function
     * @param mapper a mapping function to apply to the value, if present
     * @return a new value describing the result of applying a mapping
     * @see Task#map(Function)
     */
    <V> Task<V> mapBlocking(Function<T, V> mapper);

    <V> void executeMapper(Supplier<Task<V>> wrapper, Function<V, T> mapper);


    /**
     * If a value is present, and the value matches the given predicate,
     * return a {@link Task} containing the value, otherwise returns an empty one
     *
     * @param predicate a predicate to apply to the value, if present
     * @return a new value containing the value of this current {@link Task}
     */
    Task<T> filter(Predicate<? super T> predicate);

    /**
     * Updates the current object of this value reference
     *
     * @param value the value to set
     * @return current value
     */
    Task<T> setResult(T value) throws ValueImmutableException;

    /**
     * Sets the immutable state of this value
     *
     * @param immutable the state if it may be modified
     */
    Task<T> setImmutable(boolean immutable);

    /**
     * Checks if this value is immutable
     * (So if it may be modified)
     */
    boolean isImmutable();

    /**
     * Checks if this instance holds an object currently
     */
    boolean isPresent();

    /**
     * Checks if this operation was done
     */
    boolean isDone();

    /**
     * If this operation was successful
     */
    boolean isSuccess();

    /**
     * Checks if this instance does not hold an object currently
     */
    boolean isNull();

    /**
     * Performs the provided action if an object is currently held
     *
     * @param value the handler to perform
     */
    void ifPresent(Consumer<T> value);

    /**
     * Waits until a value is provided in this value
     *
     * @param value the handler to perform
     */
    void whenPresent(WrapperListener<T> value);

    /**
     * Adds an update listener as {@link Consumer} to this value
     * that handles this whole value instance when updating
     *
     * @param listener the listener as consumer to add
     * @return current value instance
     */
    Task<T> addUpdateListener(Consumer<Task<T>> listener);

    /**
     * Adds a simple updating listener as {@link Consumer} to this value
     *
     * @param listener the listener as consumer to add
     * @return current value instance
     */
    Task<T> addSimpleUpdateListener(Consumer<T> listener);

    /**
     * Waits until a value is provided in this value
     *
     * @param value      the handler to perform
     * @param checkSleep the time to sleep before checking again
     */
    void whenPresent(int checkSleep, WrapperListener<T> value);

    /**
     * Performs the provided action if no object is currently being held
     *
     * @param consumer the action to perform
     */
    void ifEmpty(Consumer<Task<T>> consumer);

}
