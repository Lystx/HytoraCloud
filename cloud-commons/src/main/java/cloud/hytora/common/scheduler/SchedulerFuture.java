package cloud.hytora.common.scheduler;

import java.util.function.Consumer;
import java.util.function.Supplier;

public interface SchedulerFuture extends Runnable {

    /**
     * Checks if task is sync
     *
     * @return boolean
     */
    boolean isSync();

    /**
     * Gets the id of this task
     *
     * @return int id
     */
    int getId();

    /**
     * Checks if cancelled
     *
     * @return boolean
     */
    boolean isCancelled();

    /**
     * Sets the cancel-state of this task
     *
     * @param cancelled boolean
     */
    void setCancelled(boolean cancelled);

    /**
     * Checks if an error occured
     *
     * @return boolean
     */
    boolean isError();

    /**
     * Adds an Exception that is going to be ignored when being thrown
     *
     * @param exceptionClass the class to ignore
     * @param <T> the generic that has to extend {@link Throwable}
     */
    @SuppressWarnings("unchecked")
    <T extends Throwable> void addIgnoreExceptionClass(Class<T>... exceptionClass);

    /**
     * Adds a listener to this future
     *
     * @param listener the listener
     * @return current task
     */
    SchedulerFuture addListener(Consumer<SchedulerFuture> listener);

    /**
     * Adds a Request when to cancel this task
     *
     * @param request the request
     */
    SchedulerFuture cancelIf(Supplier<Boolean> request);
}
