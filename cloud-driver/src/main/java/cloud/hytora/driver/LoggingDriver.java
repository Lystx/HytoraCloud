package cloud.hytora.driver;

public interface LoggingDriver {


    default void info(String msg, Object... args) {
        CloudDriver.getInstance().getLogger().info(msg, args);
    }
    default void warn(String msg, Object... args) {
        CloudDriver.getInstance().getLogger().warn(msg, args);
    }
    default void debug(String msg, Object... args) {
        CloudDriver.getInstance().getLogger().debug(msg, args);
    }
    default void error(String msg, Object... args) {
        CloudDriver.getInstance().getLogger().error(msg, args);
    }
    default void trace(String msg, Object... args) {
        CloudDriver.getInstance().getLogger().trace(msg, args);
    }
}
