package cloud.hytora.common.scheduler;

import cloud.hytora.common.scheduler.def.DefaultScheduler;

import java.util.List;
import java.util.function.Supplier;

public interface Scheduler {

    static Scheduler runTimeScheduler() {
        return DefaultScheduler.INSTANCE;
    }

    /**
     * Gets a {@link SchedulerFuture} by its id
     *
     * @param id the id to search for
     * @return task or null if not found
     */
    SchedulerFuture getTask(int id);

    /**
     * Gets a list of all pending {@link SchedulerFuture}s
     *
     * @return list of tasks
     */
    List<SchedulerFuture> getTasks();

    /**
     * Cancels a task by its id
     *
     * @param id the id
     * @deprecated use {@link Scheduler#cancelTask(SchedulerFuture)}
     */
    @Deprecated
    void cancelTask(int id);

    /**
     * Cancels a task
     *
     * @param task the task to cancel
     */
    void cancelTask(SchedulerFuture task);

    /**
     * Cancels all tasks
     */
    void cancelAllTasks();


    /**
     * Public Method that tries to execute a given {@link Runnable} if a provided {@link Supplier} returns {@code true} <br>
     * or until the provided timeout in milliseconds has expired from the start of the operation
     * <br> <br>
     *
     * @param runnable the runnable to execute
     * @param request  the condition that has to be true
     * @param timeOut  the timeOut for this request in milliseconds
     */
    void executeIf(Runnable runnable, Supplier<Boolean> request, long timeOut);

    /**
     * Executes a given {@link Runnable} if a provided {@link Supplier} returns {@code true} <br>
     * with a default timeout of <b>1 DAY</b>
     * <br> <br>
     *
     * @param runnable the runnable to execute
     * @param request  the condition that has to be true
     * @see #executeIf(Runnable, Supplier, long)
     */
    void executeIf(Runnable runnable, Supplier<Boolean> request);

    /**
     * Repeats a task for a given amount of time
     * This is executed synchronously
     *
     * @param task the runnable task
     * @param delay the delay between every execution
     * @param period the period
     * @param times the amount of times
     * @return task
     */
    SchedulerFuture scheduleRepeatingTaskForTimes(Runnable task, long delay, long period, long times);

    /**
     * Repeats a task for a given amount of time
     * This is executed asynchronously
     *
     * @param task the runnable task
     * @param delay the delay between every execution
     * @param period the period
     * @param times the amount of times
     * @return task
     */
    SchedulerFuture scheduleRepeatingTaskAsync(Runnable task, long delay, long period, long times);

    /**
     * Repeats a task
     * This is executed synchronously
     *
     * @param task the runnable task
     * @param delay the delay between every execution
     * @param period the period
     * @return task
     */
    SchedulerFuture scheduleRepeatingTask(Runnable task, long delay, long period);

    /**
     * Repeats a task
     * This is executed asynchronously
     *
     * @param task the runnable task
     * @param delay the delay between every execution
     * @param period the period
     * @return task
     */
    SchedulerFuture scheduleRepeatingTaskAsync(Runnable task, long delay, long period);

    /**
     * Runs a task
     * This is executed synchronously
     *
     * @param task the runnable task
     * @return scheduled task
     */
    SchedulerFuture runTask(Runnable task);

    /**
     * Runs a task
     * This is executed asynchronously
     *
     * @param task the runnable task
     * @return scheduled task
     */
    SchedulerFuture runTaskAsync(Runnable task);

    /**
     * Delays a task
     * This is executed synchronously
     *
     * @param task the task
     * @param delay the delay as Minecraft-Ticks (20 ticks = 1 Second)
     * @return scheduled task
     */
    SchedulerFuture scheduleDelayedTask(Runnable task, long delay);

    /**
     * Delays a task
     * This is executed asynchronously
     *
     * @param task the task
     * @param delay the delay as Minecraft-Ticks (20 ticks = 1 Second)
     * @return scheduled task
     */
    SchedulerFuture scheduleDelayedTaskAsync(Runnable task, long delay);

    /**
     * Searches for a free task id
     * If the id is already in use, it will generate a new one
     * until one id is free to use
     *
     * @return id as int
     */
    int generateTaskId();

}
