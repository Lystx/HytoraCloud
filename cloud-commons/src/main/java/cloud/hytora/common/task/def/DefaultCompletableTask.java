
package cloud.hytora.common.task.def;

import cloud.hytora.common.task.IPromise;
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
public class DefaultCompletableTask<T> implements IPromise<T> {

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
     * If only one value is being accepted
     */
    private boolean acceptSingleValue;

    /**
     * The latches to lock and unlock
     */
    private final Collection<CountDownLatch> countDownLatches;

    /**
     * All update listeners
     */
    private final Collection<Consumer<IPromise<T>>> updateListeners;

    public DefaultCompletableTask(T value) {
        this();
        this.heldValue = value;
    }

    public DefaultCompletableTask() {
        this.heldValue = null;
        this.immutable = false;
        this.countDownLatches = new ArrayList<>();
        this.updateListeners = new ArrayList<>();
    }

    @Override
    public void setAcceptSingleValue() {
        this.acceptSingleValue = true;
    }

    @Override
    public boolean isSuccess() {
        return throwable == null;
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
    public IPromise<T> setFailure(Throwable ex) {
        this.throwable = ex;
        this.done = true;
        this.releaseLocks();
        return this;
    }

    @Override
    public IPromise<T> setImmutable(boolean immutable) {
        this.immutable = immutable;
        return this;
    }

    @Override
    public IPromise<T> setResult(T newValue) throws ValueImmutableException {
        if (acceptSingleValue && heldValue != null) {
            return this;
        }
        if (newValue == null && this.denyNull) {
            return this;
        }
        if (this.immutable && this.heldValue != null) {
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
    public IPromise<T> registerListener(Consumer<IPromise<T>> listener) {
        if (this.isDone()) {
            listener.accept(this);
            return this;
        }
        this.updateListeners.add(listener);
        return this;
    }

    @Override
    public void ifEmpty(Consumer<IPromise<T>> consumer) {
        if (isPresent()) {
            return;
        }
        consumer.accept(this);
    }


    @Override
    public T orElse(T t) {
        return this.isNull() ? t : this.heldValue;
    }


    private TimeUnit timeOutUnit = TimeUnit.DAYS;
    private int timeOutValue = 365;
    private T fallbackValue = null;


    @Override
    public IPromise<T> timeOut(TimeUnit unit, int timeOut, T fallbackValue) {
        this.timeOutUnit = unit;
        this.timeOutValue = timeOut;
        this.fallbackValue = fallbackValue;
        return this;
    }


    @Override
    public IPromise<T> syncUninterruptedly() {
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
    public IPromise<T> onTaskSucess(Consumer<T> listener) {
        return this.registerListener(v -> {
            try {
                listener.accept(this.get());
            } catch (ValueHoldsNoObjectException e) {
                listener.accept(null);
            }
        });
    }


    @Override
    public IPromise<T> onTaskFailed(Consumer<Throwable> e) {
        return this.registerListener(v -> {
            if (v.error() != null) {
                e.accept(v.error());
            }
        });
    }

    @Override
    public T orGet(Supplier<? extends T> other) {
        return this.isNull() ? other.get() : this.heldValue;
    }

    @Override
    public IPromise<T> filter(Predicate<? super T> predicate) {
        return isNull() ? this : predicate.test(heldValue) ? this : IPromise.empty();
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
    public <E> E mapOrElse(Function<T, E> mapper, Supplier<E> ifNull) {
        if (this.isNull()) {
            return ifNull.get();
        }
        return mapper.apply(this.heldValue);
    }

    @Override
    public <V> IPromise<V> map(Function<T, V> mapper) {
        IPromise<V> task = IPromise.newInstance(this.heldValue == null ? null : mapper.apply(this.heldValue));
        if (isDone()) return task;

        this.registerListener(tTask -> {
            if (tTask.isSuccess()) {
                task.setResult(mapper.apply(this.heldValue));
            } else {
                task.setFailure(tTask.error());
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
