package cloud.hytora.common.logging;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.internal.FallbackLogger;
import cloud.hytora.common.logging.internal.SimpleLogger;
import cloud.hytora.common.logging.internal.factory.ConstantLoggerFactory;
import cloud.hytora.common.logging.internal.factory.DefaultLoggerFactory;
import cloud.hytora.common.logging.internal.factory.Slf4jLoggerFactory;
import cloud.hytora.common.misc.ReflectionUtils;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;


public abstract class Logger {


    @Setter @Getter
    private static BiSupplier<String, String> messageFormater = null;


    public static String formatMessage(String input) {
        if (messageFormater == null) {
            return input;
        }
        return messageFormater.supply(input);
    }


    private static LoggerFactory factory;

    @Setter
    @Getter
    private boolean cacheEntires;

    private Map<LogLevel, Collection<LogEntry>> cachedEntries;



    public Logger() {
        constantInstance = this;

        this.cacheEntires = false;
        this.cachedEntries = new HashMap<>();

        for (LogLevel logLevel : LogLevel.values()) {
            this.cachedEntries.put(logLevel, new ArrayList<>());
        }
    }

    static {
        try {
            Class.forName("org.slf4j.impl.StaticLoggerBinder");
            factory = new Slf4jLoggerFactory();
        } catch (ClassNotFoundException e) { // there was no static logger binder (SLF4J pre-1.8.x)
            try {
                Class<?> serviceProviderInterface = Class.forName("org.slf4j.spi.SLF4JServiceProvider");
                // check if there is a service implementation for the service, indicating a provider for SLF4J 1.8.x+ is installed
                boolean b = ServiceLoader.load(serviceProviderInterface).iterator().hasNext();
                factory = (b ? new DefaultLoggerFactory(SimpleLogger::new) : new DefaultLoggerFactory(FallbackLogger::new));
            } catch (ClassNotFoundException ex) { // there was no service provider interface (SLF4J 1.8.x+)
                factory = new DefaultLoggerFactory(FallbackLogger::new);
            }
        }
    }

    public Collection<LogEntry> getCachedEntries(LogLevel level) {
        if (level == LogLevel.ALL) {
            Collection<LogEntry> entries = new ArrayList<>();
            for (Collection<LogEntry> value : cachedEntries.values()) {
                entries.addAll(value);
            }

            return entries.stream().sorted(Comparator.comparingLong(value -> value.getTimestamp().toEpochMilli())).collect(Collectors.toList());

        }
        return cachedEntries.get(level);
    }


    public void cacheEntry(LogEntry entry) {
        Collection<LogEntry> logEntries = cachedEntries.get(entry.getLevel());
        logEntries.add(entry);
        this.cachedEntries.put(entry.getLevel(), logEntries);
    }


    public static void setFactory(@Nonnull Logger logger) {
        factory = new ConstantLoggerFactory(logger);
    }

    @Nonnull
    @CheckReturnValue
    public static Logger newInstance() {
        return factory.forName(ReflectionUtils.getCaller().getSimpleName());
    }


    private static Logger constantInstance;
    public static Logger constantInstance() {
        return constantInstance;
    }

    public abstract Logger translateColors();

    public abstract void log(@Nonnull LogLevel level, @Nullable String message, @Nonnull Object... args);

    public void error(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.ERROR, message, args);
    }

    public void warn(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.WARN, message, args);
    }

    public void info(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.INFO, message, args);
    }

    public void status(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.STATUS, message, args);
    }

    public void debug(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.DEBUG, message, args);
    }

    public void trace(@Nullable String message, @Nonnull Object... args) {
        log(LogLevel.TRACE, message, args);
    }

    public boolean isLevelEnabled(@Nonnull LogLevel level) {
        return level.isEnabled(getMinLevel());
    }

    @Nonnull
    @CheckReturnValue
    public abstract LogLevel getMinLevel();

    @Nonnull
    @CheckReturnValue
    public abstract Logger setMinLevel(@Nonnull LogLevel level);

    @Nonnull
    @CheckReturnValue
    public PrintStream asPrintStream(@Nonnull LogLevel level) {
        try {
            return new PrintStream(new LogOutputStream(this, level), true, StandardCharsets.UTF_8.name());
        } catch (Exception ex) {
            throw new WrappedException(ex);
        }
    }

}
