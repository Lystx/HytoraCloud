package cloud.hytora.common.logging;

import cloud.hytora.common.logging.internal.WrappedSlf4jLogger;
import cloud.hytora.common.collection.WrappedException;
import cloud.hytora.common.logging.internal.FallbackLogger;
import cloud.hytora.common.logging.internal.SimpleLogger;
import cloud.hytora.common.logging.internal.factory.ConstantLoggerFactory;
import cloud.hytora.common.logging.internal.factory.DefaultLoggerFactory;
import cloud.hytora.common.logging.internal.factory.Slf4jLoggerFactory;
import cloud.hytora.common.misc.ReflectionUtils;

import javax.annotation.CheckReturnValue;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ServiceLoader;


public abstract class Logger {

    private static LoggerFactory factory;

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

    public static void setFactory(@Nonnull Logger logger) {
        factory = new ConstantLoggerFactory(logger);
    }

    @Nonnull
    @CheckReturnValue
    public static Logger newInstance() {
        return factory.forName(ReflectionUtils.getCaller().getSimpleName());
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
