package cloud.hytora.node.console.log4j;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class EmptyAppenderSkeleton extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent loggingEvent) {
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
