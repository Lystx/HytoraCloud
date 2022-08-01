
package cloud.hytora.common.task.def;

import cloud.hytora.common.task.Task;
import cloud.hytora.common.task.WrapperListener;
import cloud.hytora.common.task.exception.ValueHoldsNoObjectException;
import cloud.hytora.common.task.exception.ValueImmutableException;
import cloud.hytora.common.task.exception.ValueTimedOutException;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;


@Getter
@Setter
public class SimpleTask<T> implements Task<T> {

    /**
     * The current value being held
     */
    private T heldValue;

    /**
     * If this value is allowed to be modified
     */
    private boolean immutable;

    /**
     * The error (if set)
     */
    private Throwable throwable;

    /**
     * If this is done
     */
    private boolean done;

    /**
     * If null values are forbidden
     */
    private boolean denyNull;

    /**
     * The latches to lock and unlock
     */
    private final Collection<CountDownLatch> countDownLatches;

    /**
     * All update listeners
     */
    private final Collection<Consumer<Task<T>>> updateListeners;

    public SimpleTask(T value) {
        this();
        this.heldValue = value;
    }

    public SimpleTask() {
        this.heldValue = null;
        this.immutable = false;
        this.countDownLatches = new ArrayList<>();
        this.updateListeners = new ArrayList<>();
    }

    @Override
    public boolean isSuccess() {
        return throwable == null;
    }

    @Override
    public Task<T> denyNull() {
        this.denyNull = true;
        return this;
    }

    @Override
    public Task<T> allowNull() {
        this.denyNull = false;
        return this;
    }

    @Override
    public Throwable error() {
        return throwable;
    }

    @Override
    public <V> V as(Class<? extends V> wrapperClass) throws ValueHoldsNoObjectException {
        V t = (V) get();
        if (t != null && wrapperClass.isAssignableFrom(t.getClass())) {
            return wrapperClass.cast(t);
        }
        return t;
    }

    @Override
    public boolean isDone() {
        if (denyNull) {
            return this.heldValue != null;
        }
        return done;
    }

    @Override
    public Task<T> setFailure(Throwable ex) {
        this.throwable = ex;
        this.done = true;
        this.releaseLocks();
        return this;
    }

    @Override
    public Task<T> setImmutable(boolean immutable) {
        this.immutable = immutable;
        return this;
    }

    @Override
    public Task<T> setResult(T newValue) throws ValueImmutableException {
        if (newValue == null && this.denyNull) {
            return this;
        }
        if (this.immutable && this.heldValue == null) {
            throw new ValueImmutableException(this);
        }
        this.heldValue = newValue;
        this.done = true;
        this.releaseLocks();
        return this;
    }

    private void releaseLocks() {
        this.updateListeners.forEach(c -> c.accept(this));

        //releasing all locks
        for (CountDownLatch latch : countDownLatches) {
            latch.countDown();
        }
        this.countDownLatches.clear();
    }

    @Override
    public T orNull() {
        return this.orElse((T) null);
    }

    @Override
    public void ifPresent(Consumer<T> consumer) {
        if (isNull()) {
            return;
        }
        consumer.accept(this.heldValue);
    }

    private final Map<UUID, Boolean> pendingQueries = new ConcurrentHashMap<>();

    @Override
    public <V> Task<V> mapBlocking(Function<T, V> mapper) {
        if (isPresent()) {
            return map(mapper);
        }

        int checkSleep = 500;

        //starting pending query
        UUID queryId = UUID.randomUUID();
        this.pendingQueries.put(queryId, true);

        while (pendingQueries.get(queryId)) {
            if (isPresent()) {
                this.pendingQueries.put(queryId, false);
                return map(mapper);
            }
            if (checkSleep != -1) {
                try {
                    Thread.sleep(checkSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        return map(mapper);
    }

    @Override
    public void whenPresent(int checkSleep, WrapperListener<T> value) {
        //if directly present -> accept instantly
        if (isPresent()) {
            value.onPresent(this.heldValue);
            return;
        }

        //starting pending query
        UUID queryId = UUID.randomUUID();
        this.pendingQueries.put(queryId, true);

        while (pendingQueries.get(queryId)) {
            if (isPresent()) {
                value.onPresent(this.heldValue);
                this.pendingQueries.put(queryId, false);
            } else {
                value.onStillNotPresent();
            }
            if (checkSleep != -1) {
                try {
                    Thread.sleep(checkSleep);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void whenPresent(WrapperListener<T> value) {
        this.whenPresent(-1, value);
    }

    @Override
    public Task<T> registerListener(Consumer<Task<T>> listener) {
        if (this.isDone()) {
            listener.accept(this);
            return this;
        }
        this.updateListeners.add(listener);
        return this;
    }

    @Override
    public void ifEmpty(Consumer<Task<T>> consumer) {
        if (isPresent()) {
            return;
        }
        consumer.accept(this);
    }

    @Override
    public T getOrPerform(Predicate<Task<T>> predicate, Consumer<Task<T>> ifTrue, Consumer<Task<T>> ifFalse) {
        if (isNull()) {
            if (predicate.test(this)) {
                ifTrue.accept(this);
            } else {
                ifFalse.accept(this);
            }
            return null;
        }
        return this.heldValue;
    }

    @Override
    public T orElse(T t) {
        return this.isNull() ? t : this.heldValue;
    }


    private TimeUnit timeOutUnit = TimeUnit.DAYS;
    private int timeOutValue = 365;
    private T fallbackValue = null;


    @Override
    public Task<T> timeOut(TimeUnit unit, int timeOut, T fallbackValue) {
        this.timeOutUnit = unit;
        this.timeOutValue = timeOut;
        this.fallbackValue = fallbackValue;
        return this;
    }


    @Override
    public Task<T> syncUninterruptedly() {
        if (isPresent() || isDone()) {
            return this;
        }

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                releaseLocks();
            }
        }, timeOutUnit.toMillis(timeOutValue));

        CountDownLatch latch = new CountDownLatch(1);
        this.countDownLatches.add(latch);

        while (latch.getCount() > 0) {
            try {
                latch.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            return this;
        } catch (ValueHoldsNoObjectException e) {
            throw new ValueTimedOutException(this);
        }
    }

    @Override
    public Task<T> onTaskSucess(Consumer<T> listener) {
        return this.registerListener(v -> {
            try {
                listener.accept(this.get());
            } catch (ValueHoldsNoObjectException e) {
                listener.accept(null);
            }
        });
    }


    @Override
    public Task<T> onTaskFailed(Consumer<Throwable> e) {
        return this.registerListener(v -> {
            if (v.error() != null) {
                e.accept(v.error());
            }
        });
    }

    @Override
    public Task<T> onTaskFailedOrNull(Consumer<Throwable> e) {
        return this.registerListener(v -> {
            if (v.error() != null || v.isNull()) {
                e.accept(v.error());
            }
        });
    }

    @Override
    public T orGet(Supplier<? extends T> other) {
        return this.isNull() ? other.get() : this.heldValue;
    }

    @Override
    public Task<T> filter(Predicate<? super T> predicate) {
        return isNull() ? this : predicate.test(heldValue) ? this : Task.empty();
    }

    @Override
    public <E extends Throwable> T orThrow(Supplier<? extends E> exception) {
        if (this.heldValue != null) {
            return this.heldValue;
        } else {
            try {
                throw exception.get();
            } catch (Throwable e) {
                //ignore throwing this
            }
        }
        return null;
    }

    @Override
    public <E extends Throwable> T orThrow(E exception) {
        if (this.heldValue != null) {
            return this.heldValue;
        } else {
            try {
                throw exception;
            } catch (Throwable e) {
                //ignore throwing this
            }
        }
        return null;
    }

    @Override
    public void orElseDo(Predicate<T> predicate, Runnable ifFalse, Consumer<T> or) {
        if (!predicate.test(this.heldValue)) {
            ifFalse.run();
            return;
        }

        or.accept(this.heldValue);
    }

    @Override
    public void ifPresentOrElse(Consumer<T> ifPresent, Runnable orElse) {
        if (this.isPresent()) {
            ifPresent.accept(this.get());
        } else {
            orElse.run();
        }
    }

    @Override
    public <V> void executeMapper(Supplier<Task<V>> wrapper, Function<V, T> mapper) {
        Task<V> wrapped = wrapper.get();

        if (this.denyNull) {
            wrapped.denyNull();
        }

        wrapped.registerListener(new Consumer<Task<V>>() {
            @Override
            public void accept(Task<V> result) {

                if (result.error() != null) {
                    setFailure(result.error());
                    return;
                }

                try {
                    setResult(mapper.apply(result.get()));
                } catch (ValueHoldsNoObjectException e) {
                    setResult(null);
                }
            }
        });
    }

    @Override
    public <E> E mapOrElse(Function<T, E> mapper, Supplier<E> ifNull) {
        if (this.isNull()) {
            return ifNull.get();
        }
        return mapper.apply(this.heldValue);
    }

    @Override
    public <V> Task<V> map(Function<T, V> mapper) {
        Task<V> task = Task.empty();
        if (this.isNull()) {
            return task;
        }
        task = Task.build(mapper.apply(this.heldValue));
        Task<V> finalTask = task;
        this.registerListener(new Consumer<Task<T>>() {
            @Override
            public void accept(Task<T> tTask) {
                if (tTask.isSuccess()) {
                    finalTask.setResult((V) tTask.get());
                } else {
                    finalTask.setFailure(tTask.error());
                }
            }
        });
        return task;
    }

    @Override
    public boolean isPresent() {
        return this.heldValue != null;
    }

    @Override
    public boolean isNull() {
        return this.heldValue == null;
    }

    @Override
    public T get() throws ValueHoldsNoObjectException {
        if (this.isNull()) {
            throw new ValueHoldsNoObjectException(this);
        }
        return this.heldValue;
    }


}
