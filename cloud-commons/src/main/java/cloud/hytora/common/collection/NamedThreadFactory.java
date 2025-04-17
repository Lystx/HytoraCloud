package cloud.hytora.common.collection;

import javax.annotation.Nonnull;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntFunction;

public class NamedThreadFactory implements ThreadFactory {

    private static final AtomicInteger poolNumber = new AtomicInteger(1);

    protected final int id = poolNumber.getAndIncrement();
    protected final IntFunction<String> nameFunction;
    protected final ThreadGroup group;
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    protected final ClassLoader classLoader;

    public NamedThreadFactory(@Nonnull IntFunction<String> nameFunction, ClassLoader classLoader) {
        SecurityManager securityManager = System.getSecurityManager();
        this.classLoader = classLoader;
        this.group = (securityManager != null) ? securityManager.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.nameFunction = nameFunction;
    }

    public NamedThreadFactory(@Nonnull String prefix) {
        this(id -> prefix + "-" + id, ClassLoader.getSystemClassLoader());
    }
    public NamedThreadFactory(@Nonnull String prefix, ClassLoader classLoader) {
        this(id -> prefix + "-" + id, classLoader);
    }

    @Override
    public Thread newThread(@Nonnull Runnable task) {
        Thread thread = new Thread(group, task, nameFunction.apply(threadNumber.getAndIncrement()));
        thread.setContextClassLoader(classLoader);
        if (thread.isDaemon()) thread.setDaemon(false);
        if (thread.getPriority() != Thread.NORM_PRIORITY) thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }

}
