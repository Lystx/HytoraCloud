package cloud.hytora.node;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Console;
import cloud.hytora.driver.event.IEventManager;
import cloud.hytora.driver.event.defaults.driver.DriverLogEvent;
import cloud.hytora.driver.node.INode;
import cloud.hytora.node.console.handler.ConsoleLogHandler;
import cloud.hytora.node.console.handler.FileLogHandler;
import cloud.hytora.node.console.jline3.JLine3Console;

import java.util.Arrays;

public class CloudBootstrap {

    public static void main(String[] args) {

        try {
            Console console = new JLine3Console("§c%node%@§f%screen% §8» §r");
            HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));

            Logger.setFactory(logger.addHandler(new ConsoleLogHandler(console), new FileLogHandler()));

            System.setOut(logger.asPrintStream(LogLevel.INFO));
            System.setErr(logger.asPrintStream(LogLevel.ERROR));

            String modulePath = Arrays.stream(args).filter(s -> s.startsWith("--moduleFolder=")).findFirst().orElse("--moduleFolder=modules/").split("=")[1];

            CloudDriver<INode> driver = new NodeDriver(logger, console, Arrays.asList(args).contains("--devMode"), modulePath);

            logger.addHandler(entry -> driver.getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(new DriverLogEvent(entry)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
