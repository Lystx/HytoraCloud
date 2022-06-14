package cloud.hytora.common;

import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.wrapper.Task;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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


    @Nonnull
    @CheckReturnValue
    public static String args(@Nullable Object messageObject, @Nonnull Object... args) {
        StringBuilder message = new StringBuilder(String.valueOf(messageObject));
        for (Object arg : args) {
            if (arg instanceof Throwable) {
                continue;
            }
            int index = message.indexOf("{}");
            if (index == -1) {
                break;
            }
            message.replace(index, index + 2, String.valueOf(arg));
        }
        return message.toString();
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

    public static <R, T extends Throwable> R perform(boolean condition, Supplier<R> ifTrue, Supplier<R> ifFalse) {
        if (condition) {
            return ifTrue.get();
        } else {
            return ifFalse.get();
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

    public static <T> Task<T> find(Collection<T> iterator, Predicate<? super T> predicate) {
        return Task.build(findOrNull(iterator, predicate));
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... objects) {
        return Arrays.asList(objects);
    }

}
