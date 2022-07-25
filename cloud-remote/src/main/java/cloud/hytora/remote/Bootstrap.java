package cloud.hytora.remote;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.remote.impl.log.DefaultLogHandler;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Arrays;

public class Bootstrap {


    private static Instrumentation instrumentationInstance;

    public static void premain(String premainArgs, Instrumentation instrumentation) {
        instrumentationInstance = instrumentation;
    }

    public static void agentmain(String agentArgs, Instrumentation instrumentation) {
        instrumentationInstance = instrumentation;
    }


    public static void main(String[] args) {

        HandledLogger logger = new HandledAsyncLogger(LogLevel.TRACE);
        logger.addHandler(new DefaultLogHandler());
        logger.addHandler(entry -> CloudDriver.getInstance().getEventManager().callEventGlobally(new DriverLogEvent(entry)));
        Logger.setFactory(logger);


        Remote remote = new Remote(RemoteIdentity.read(new File("property.json")), logger, instrumentationInstance, args);

        try {
            remote.startApplication();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
