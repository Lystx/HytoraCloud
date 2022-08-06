package cloud.hytora.remote;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.remote.impl.log.DefaultLogHandler;
import lombok.var;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import static cloud.hytora.driver.CloudDriver.SERVER_PUBLISH_INTERVAL;

public class Bootstrap {


    private static Instrumentation instrumentationInstance;

    public static void premain(String premainArgs, Instrumentation instrumentation) {
        instrumentationInstance = instrumentation;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        instrumentationInstance = instrumentation;
    }


    public static void main(String[] args) {
        var identity = RemoteIdentity.read(new File("property.json"));
        var logger = new HandledAsyncLogger(identity.getLogLevel());
        logger.addHandler(new DefaultLogHandler());
        logger.addHandler(entry -> CloudDriver.getInstance().getEventManager().callEventGlobally(new DriverLogEvent(entry)));
        Logger.setFactory(logger);


        var remote = new Remote(identity, logger, instrumentationInstance, args);

        try {
            remote.startApplication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
