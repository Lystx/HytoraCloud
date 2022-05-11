package cloud.hytora.common.logging.internal;

import cloud.hytora.common.logging.LogLevel;
import org.slf4j.Marker;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static cloud.hytora.common.logging.LogLevel.DEBUG;
import static cloud.hytora.common.logging.LogLevel.INFO;


public class SimpleLogger extends FallbackLogger implements org.slf4j.Logger {

	public SimpleLogger(@Nullable String name) {
		super(name);
	}

	@Override
	public void error(@Nullable String message, @Nonnull Object... args) {
		super.error(message, args);
	}

	@Override
	public void warn(@Nullable String message, @Nonnull Object... args) {
		super.warn(message, args);
	}

	@Override
	public void info(@Nullable String message, @Nonnull Object... args) {
		super.info(message, args);
	}

	@Override
	public void status(@Nullable String message, @Nonnull Object... args) {
		super.status(message, args);
	}

	@Override
	public void debug(@Nullable String message, @Nonnull Object... args) {
		super.debug(message, args);
	}

	@Override
	public void trace(@Nullable String message, @Nonnull Object... args) {
		super.trace(message, args);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean isTraceEnabled() {
		return isLevelEnabled(LogLevel.TRACE);
	}

	@Override
	public void trace(String msg) {
		trace(msg, new Object[0]);
	}

	@Override
	public void trace(String format, Object arg) {
		trace(format, new Object[] { arg });
	}

	@Override
	public void trace(String format, Object arg1, Object arg2) {
		trace(format, new Object[] { arg1, arg2 });
	}

	@Override
	public void trace(String msg, Throwable t) {
		trace(msg, new Object[] { t });
	}

	@Override
	public boolean isTraceEnabled(Marker marker) {
		return isLevelEnabled(LogLevel.TRACE);
	}

	@Override
	public void trace(Marker marker, String msg) {
		trace(msg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg) {
		trace(format, arg);
	}

	@Override
	public void trace(Marker marker, String format, Object arg1, Object arg2) {
		trace(format, arg1, arg2);
	}

	@Override
	public void trace(Marker marker, String format, Object... argArray) {
		trace(format, argArray);
	}

	@Override
	public void trace(Marker marker, String msg, Throwable t) {
		trace(msg, t);
	}

	@Override
	public boolean isDebugEnabled() {
		return isLevelEnabled(DEBUG);
	}

	@Override
	public void debug(String msg) {
		debug(msg, new Object[0]);
	}

	@Override
	public void debug(String format, Object arg) {
		debug(format, new Object[] { arg });
	}

	@Override
	public void debug(String format, Object arg1, Object arg2) {
		debug(format, new Object[] { arg1, arg2 });
	}

	@Override
	public void debug(String msg, Throwable t) {
		debug(msg, new Object[] { t });
	}

	@Override
	public boolean isDebugEnabled(Marker marker) {
		return isLevelEnabled(DEBUG);
	}

	@Override
	public void debug(Marker marker, String msg) {
		debug(msg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg) {
		debug(format, arg);
	}

	@Override
	public void debug(Marker marker, String format, Object arg1, Object arg2) {
		debug(format, arg1, arg2);
	}

	@Override
	public void debug(Marker marker, String format, Object... arguments) {
		debug(format, arguments);
	}

	@Override
	public void debug(Marker marker, String msg, Throwable t) {
		debug(msg, t);
	}

	@Override
	public boolean isInfoEnabled() {
		return isLevelEnabled(INFO);
	}

	@Override
	public void info(String msg) {
		info(msg, new Object[0]);
	}

	@Override
	public void info(String format, Object arg) {
		info(format, new Object[] { arg });
	}

	@Override
	public void info(String format, Object arg1, Object arg2) {
		info(format, new Object[] { arg1, arg2 });
	}

	@Override
	public void info(String msg, Throwable t) {
		info(msg, new Object[] { t });
	}

	@Override
	public boolean isInfoEnabled(Marker marker) {
		return isLevelEnabled(INFO);
	}

	@Override
	public void info(Marker marker, String msg) {
		info(msg);
	}

	@Override
	public void info(Marker marker, String format, Object arg) {
		info(format, arg);
	}

	@Override
	public void info(Marker marker, String format, Object arg1, Object arg2) {
		info(format, arg1, arg2);
	}

	@Override
	public void info(Marker marker, String format, Object... arguments) {
		info(format, arguments);
	}

	@Override
	public void info(Marker marker, String msg, Throwable t) {
		info(msg, t);
	}

	@Override
	public boolean isWarnEnabled() {
		return isLevelEnabled(LogLevel.WARN);
	}

	@Override
	public void warn(String msg) {
		warn(msg, new Object[0]);
	}

	@Override
	public void warn(String format, Object arg) {
		warn(format, new Object[] { arg });
	}

	@Override
	public void warn(String format, Object arg1, Object arg2) {
		warn(format, new Object[] { arg1, arg2 });
	}

	@Override
	public void warn(String msg, Throwable t) {
		warn(msg, new Object[] { t });
	}

	@Override
	public boolean isWarnEnabled(Marker marker) {
		return isLevelEnabled(LogLevel.WARN);
	}

	@Override
	public void warn(Marker marker, String msg) {
	}

	@Override
	public void warn(Marker marker, String format, Object arg) {
		warn(format, arg);
	}

	@Override
	public void warn(Marker marker, String format, Object arg1, Object arg2) {
		warn(format, arg1, arg2);
	}

	@Override
	public void warn(Marker marker, String format, Object... arguments) {
		warn(format, arguments);
	}

	@Override
	public void warn(Marker marker, String msg, Throwable t) {
		warn(msg, t);
	}

	@Override
	public boolean isErrorEnabled() {
		return isLevelEnabled(LogLevel.ERROR);
	}

	@Override
	public void error(String msg) {
		error(msg, new Object[0]);
	}

	@Override
	public void error(String format, Object arg) {
		error(format, new Object[] { arg });
	}

	@Override
	public void error(String format, Object arg1, Object arg2) {
		error(format, new Object[] { arg1, arg2 });
	}

	@Override
	public void error(String msg, Throwable t) {
		error(msg, new Object[] { t });
	}

	@Override
	public boolean isErrorEnabled(Marker marker) {
		return isLevelEnabled(LogLevel.ERROR);
	}

	@Override
	public void error(Marker marker, String msg) {
		error(msg);
	}

	@Override
	public void error(Marker marker, String format, Object arg) {
		error(format, arg);
	}

	@Override
	public void error(Marker marker, String format, Object arg1, Object arg2) {
		error(format, arg1, arg2);
	}

	@Override
	public void error(Marker marker, String format, Object... arguments) {
		error(format, arguments);
	}

	@Override
	public void error(Marker marker, String msg, Throwable t) {
		error(msg, t);
	}

}
