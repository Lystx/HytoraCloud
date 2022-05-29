package cloud.hytora.common;

import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.wrapper.Wrapper;
import lombok.experimental.UtilityClass;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DriverUtility {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }

    public static void print(Object message) {
        System.out.println(message);
    }

    public static void perform(boolean condition, ExceptionallyRunnable run) {
        if (condition) run.run();
    }

    public static <T extends Throwable> void perform(boolean condition, Runnable ifTrue, T throwIfFalse) {
        if (condition) {
            ifTrue.run();
        } else {
            try {
                throw throwIfFalse;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    public static <R, T extends Throwable> R perform(boolean condition, Supplier<R> ifTrue, T throwIfFalse) {
        if (condition) {
            return ifTrue.get();
        } else {
            try {
                throw throwIfFalse;
            } catch (Throwable e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static void perform(boolean condition, Runnable ifTrue, Runnable ifFalse) {
        if (condition) {
            ifTrue.run();
        } else {
            ifFalse.run();
        }
    }


    public static <T> T findOrNull(Collection<T> iterator, Predicate<? super T> predicate) {
        return iterator.stream().filter(predicate).findFirst().orElse(null);
    }

    public static <T> Wrapper<T> find(Collection<T> iterator, Predicate<? super T> predicate) {
        return Wrapper.build(findOrNull(iterator, predicate));
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... objects) {
        return Arrays.asList(objects);
    }

}
