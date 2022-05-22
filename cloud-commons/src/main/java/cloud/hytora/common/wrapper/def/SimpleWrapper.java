
package cloud.hytora.common.wrapper.def;

import cloud.hytora.common.wrapper.Wrapper;
import cloud.hytora.common.wrapper.WrapperListener;
import cloud.hytora.common.wrapper.exception.ValueHoldsNoObjectException;
import cloud.hytora.common.wrapper.exception.ValueImmutableException;
import cloud.hytora.common.wrapper.exception.ValueTimedOutException;
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
public class SimpleWrapper<T> implements Wrapper<T> {

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
    private final Collection<Consumer<Wrapper<T>>> updateListeners;

    public SimpleWrapper() {
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
    public Wrapper<T> denyNull() {
        this.denyNull = true;
        return this;
    }

    @Override
    public Wrapper<T> allowNull() {
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
    public Wrapper<T> setFailure(Throwable ex) {
        this.throwable = ex;
        this.releaseLocks();
        return this;
    }

    @Override
    public Wrapper<T> setImmutable(boolean immutable) {
        this.immutable = immutable;
        return this;
    }

    @Override
    public Wrapper<T> setResult(T newValue) throws ValueImmutableException {
        if (newValue == null && this.denyNull) {
            return this;
        }
        if (this.immutable && this.heldValue == null) {
            throw new ValueImmutableException(this);
        }
        this.heldValue = newValue;
        this.releaseLocks();
        return this;
    }

    private void releaseLocks() {
        this.updateListeners.forEach(c -> c.accept(this));
        this.done = true;

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
    public <V> Wrapper<V> mapBlocking(Function<T, V> mapper) {
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
    public Wrapper<T> addUpdateListener(Consumer<Wrapper<T>> listener) {
        if (this.isDone()) {
            listener.accept(this);
            return this;
        }
        this.updateListeners.add(listener);
        return this;
    }

    @Override
    public void ifEmpty(Consumer<Wrapper<T>> consumer) {
        if (isPresent()) {
            return;
        }
        consumer.accept(this);
    }

    @Override
    public T getOrPerform(Predicate<Wrapper<T>> predicate, Consumer<Wrapper<T>> ifTrue, Consumer<Wrapper<T>> ifFalse) {
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
    public Wrapper<T> timeOut(TimeUnit unit, int timeOut, T fallbackValue) {
        this.timeOutUnit = unit;
        this.timeOutValue = timeOut;
        this.fallbackValue = fallbackValue;
        return this;
    }

    @Override
    public Wrapper<T> syncUninterruptedly() {
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
    public Wrapper<T> addSimpleUpdateListener(Consumer<T> listener) {
        return this.addUpdateListener(v -> {
            try {
                listener.accept(v.get());
            } catch (ValueHoldsNoObjectException e) {
                listener.accept(null);
            }
        });
    }

    @Override
    public T orGet(Supplier<? extends T> other) {
        return this.isNull() ? other.get() : this.heldValue;
    }

    @Override
    public Wrapper<T> filter(Predicate<? super T> predicate) {
        return isNull() ? this : predicate.test(heldValue) ? this : Wrapper.empty();
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
    public <V> void executeMapper(Supplier<Wrapper<V>> wrapper, Function<V, T> mapper) {
        Wrapper<V> wrapped = wrapper.get();

        if (this.denyNull) {
            wrapped.denyNull();
        }

        wrapped.addUpdateListener(new Consumer<Wrapper<V>>() {
            @Override
            public void accept(Wrapper<V> result) {

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
    public <V> Wrapper<V> map(Function<T, V> mapper) {
        Wrapper<V> wrapper = Wrapper.empty();
        if (this.isNull()) {
            return wrapper;
        }
        wrapper = Wrapper.build(mapper.apply(this.heldValue));
        Wrapper<V> finalWrapper = wrapper;
        this.addUpdateListener(new Consumer<Wrapper<T>>() {
            @Override
            public void accept(Wrapper<T> tWrapper) {
                if (tWrapper.isSuccess()) {
                    finalWrapper.setResult((V) tWrapper.get());
                } else {
                    finalWrapper.setFailure(tWrapper.error());
                }
            }
        });
        return wrapper;
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
