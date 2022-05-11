package cloud.hytora.node;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.common.logging.handler.HandledLogger;
import cloud.hytora.driver.command.Console;
import cloud.hytora.node.NodeDriver;
import cloud.hytora.node.console.handler.ConsoleLogHandler;
import cloud.hytora.node.console.handler.FileLogHandler;
import cloud.hytora.node.console.jline3.JLine3Console;

public class CloudBootstrap {

    public static void main(String[] args) {

        try {
            Console console = new JLine3Console("§8| §bCloud §8» §b%screen% §8» §r");
            console.setLineCaching(true);
            HandledLogger logger = new HandledAsyncLogger(LogLevel.fromName(System.getProperty("cloud.logging.level", "INFO")));
            Logger.setFactory(logger.addHandler(new ConsoleLogHandler(console), new FileLogHandler()));

            System.setOut(logger.asPrintStream(LogLevel.INFO));
            System.setErr(logger.asPrintStream(LogLevel.ERROR));

            new NodeDriver(logger, console);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
