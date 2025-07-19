package cloud.hytora.node.impl;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class Log4JAppender extends AppenderSkeleton {
    @Override
    protected void append(LoggingEvent event) {

    }

    @Override
    public void close() {

    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
