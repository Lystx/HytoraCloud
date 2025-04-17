package cloud.hytora.common;

import cloud.hytora.common.collection.NamedThreadFactory;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.function.ExceptionallyRunnable;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.scheduler.Scheduler;
import cloud.hytora.common.task.Task;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class DriverUtility {

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }


    public static Task<Path> downloadVersion(String urlStr, Path location, ProgressBar pb) {
        Task<Path> task = Task.empty();

        Task.runAsync(() -> {
            try {
                URL url = new URL(urlStr);
                String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
                URLConnection con = url.openConnection();
                con.setRequestProperty("User-Agent", USER_AGENT);

                int contentLength = con.getContentLength();
                InputStream inputStream = con.getInputStream();

                OutputStream outputStream = Files.newOutputStream(location);
                byte[] buffer = new byte[2048];
                int length;
                int downloaded = 0;

                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                    downloaded += length;
                    pb.stepTo((long) ((downloaded * 100L) / (contentLength * 1.0)));
                }
                outputStream.close();
                inputStream.close();
                pb.close("");
                task.setResult(location);
            } catch (Exception e) {
                task.setFailure(e);
            }
        });
        return task;
    }

    public static Task<Void> downloadVersion(String urlStr, Path location) {
        Task<Void> task = Task.empty();
        try {
            ProgressBar pb = new ProgressBar(ProgressBarStyle.UNICODE_BLOCK, 300);

            pb.setPrintAutomatically(true);
            pb.setExpandingAnimation(false);

            URL url = new URL(urlStr);
            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);

            int contentLength = con.getContentLength();
            InputStream inputStream = con.getInputStream();

            OutputStream outputStream = Files.newOutputStream(location);
            byte[] buffer = new byte[2048];
            int length;
            int downloaded = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                downloaded += length;
                pb.stepTo((long) ((downloaded * 100L) / (contentLength * 1.0)));
            }
            pb.setExtraMessage("Cleaning up...");
            outputStream.close();
            inputStream.close();
            pb.close("");
            task.setResult(null);
        } catch (Exception e) {
            task.setFailure(e);
        }
        return task;
    }

    public static boolean hasInternetConnection() {
        try {
            URL googleUrl = new URL("https://www.google.com");
            URLConnection connection = googleUrl.openConnection();
            connection.connect();
            connection.getInputStream().close();
            return true;
        } catch (MalformedURLException e) {
            throw WrappedException.silent(e);
        } catch (IOException e) {
            return false;
        }
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

    public static InputStream readInputStreamFromURL(String url) throws IOException {
        URLConnection urlConnection = new URL(url).openConnection();

        urlConnection.setUseCaches(false);
        urlConnection.setDoOutput(false);

        urlConnection.setConnectTimeout(5000);
        urlConnection.setReadTimeout(5000);

        urlConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
        urlConnection.connect();

        return urlConnection.getInputStream();
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

    public static <T> List<T> newList() {
        return new ArrayList<T>();
    }

    @SafeVarargs
    public static <T> List<T> listOf(T... objects) {
        return new ArrayList<>(Arrays.asList(objects));
    }

}
