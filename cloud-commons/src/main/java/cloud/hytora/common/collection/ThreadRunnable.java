package cloud.hytora.common.collection;


import cloud.hytora.common.function.ExceptionallyRunnable;

import java.io.Closeable;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class ThreadRunnable implements Runnable, Closeable {

    /**
     * The executor for this runnable
     */
    private final ScheduledThreadPoolExecutor executor;

    /**
     * The wrapped runnable
     */
    private final ExceptionallyRunnable runnable;

    /**
     * All cached tasks
     */
    private final Set<CompletableFuture<?>> tasks;

    public ThreadRunnable(ExceptionallyRunnable runnable) {
        this.runnable = runnable;
        this.executor = new ScheduledThreadPoolExecutor(1);
        this.tasks = new HashSet<>();
    }

    /**
     * Allows to define the desired number of threads.
     *
     * @param threads The number of simultaneous threads / executions.
     */
    public ThreadRunnable setThreads(int threads) {
        if (this.executor.isShutdown()) {
            return this;
        }
        executor.setCorePoolSize(threads);
        return this;
    }

    /**
     * Run without blocking.
     */
    protected void runInSameThread() {
        try {
            if (this.executor.isShutdown()) {
                return;
            }
            this.runnable.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run without blocking.
     *
     * @param then Then.
     * @param catcher Catch.
     */
    protected void runInSameThread(Runnable then, Runnable catcher) {
        try {
            if (this.executor.isShutdown()) {
                return;
            }
            this.runnable.run();
            then.run();
        } catch (final Throwable throwable) {
            catcher.run();
        }
    }



    /**
     * It is semi-multi-threaded with a blocking function,
     * i.e. it is like an ordinary Runnable,
     * but we can stop all its executions with forceClose()
     */
    public void run() {
        if (this.executor.isShutdown()) {
            return;
        }

        CompletableFuture<?> future = new CompletableFuture<>();
        this.tasks.add(future);

        executor.execute(() -> {
            this.runInSameThread();
            future.complete(null);
        });

        future.join();
        this.tasks.remove(future);

    }

    /**
     * Allows to bring the concept then/catch async of JavaScript.
     * The method is blocking.
     * The executions can be stopped with forceClose().
     *
     * @param then Then.
     * @param catcher Catch.
     */
    public ThreadRunnable run(Runnable then, Runnable catcher) {
        if (this.executor.isShutdown()) {
            return this;
        }
        CompletableFuture<?> future = new CompletableFuture<>();
        this.tasks.add(future);

        this.executor.execute(() -> {
            try {
                this.runnable.run();
                future.complete(null);
                then.run();
            } catch (Throwable throwable) {
                future.complete(null);
                catcher.run();
            }
        });

        future.join();
        this.tasks.remove(future);
        return this;
    }

    /**
     * The method is async.
     * Executions can be stopped with forceClose().
     * @return The Future of task.
     */
    public Future<?> runAsync(){
        if (this.executor.isShutdown()) {
            return null;
        }
        return this.executor.submit((Runnable) this::runInSameThread);
    }

    /**
     *
     * Allows to bring the concept then/catch async of JavaScript.
     *
     * The method is async.
     * Executions can be stopped with forceClose().
     *
     * @param then Then.
     * @param catcher Catch.
     * @return The Future of task.
     */
    public Future<?> runAsync(Runnable then, Runnable catcher) {
        if (this.executor.isShutdown()) {
            return null;
        }
        return this.executor.submit(() -> {
            runInSameThread(then, catcher);
        });
    }

    /**
     * Run the task every x ms.
     *
     * The method is async,
     * Executions can be cancelled by close() and can be stopped with forceClose().
     *
     * @param ms The periode of execution.
     * @return The ScheduledFuture of task.
     */
    public ScheduledFuture<?> runRepeated(int ms) {
        if (this.executor.isShutdown()) {
            return null;
        }
        return runRepeated(0, ms);
    }

    /**
     * Run the task every x ms, after x ms.
     *
     * The method is async,
     * Executions can be cancelled by close() and can be stopped with forceClose().
     *
     * @param wait The time before starting repeated-task.
     * @param ms The periode of execution.
     * @return The ScheudledFuture of task.
     */
    public ScheduledFuture<?> runRepeated(int wait, int ms) {
        if (this.executor.isShutdown()) {
            return null;
        }
        return this.executor.scheduleAtFixedRate(this::runInSameThread, wait, ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Run the task after x ms.
     *
     * The method is async,
     * Executions can be cancelled by close() and can be stopped with forceClose().
     *
     * @param ms Timeout before execution.
     * @return The ScheduledFuture of task.
     */
    public ScheduledFuture<?> runAfter(int ms) {
        if (this.executor.isShutdown()) {
            return null;
        }
        return this.executor.schedule((Runnable) this::runInSameThread, ms, TimeUnit.MILLISECONDS);
    }

    /**
     * Close new tasks as well as running tasks.
     */
    public void forceClose() {
        this.executor.shutdownNow();
        tasks.forEach(t -> {
            t.complete(null);
        });
    }

    /**
     * Close new tasks, while letting current tasks run.
     */
    @Override
    public void close() {
        this.executor.shutdown();
    }
}
