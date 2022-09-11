package cloud.hytora.database.bootstrap;

import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.UncoloredMessageFormatter;
import cloud.hytora.common.logging.handler.HandledAsyncLogger;
import cloud.hytora.database.DocumentDatabase;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.HttpServer;
import cloud.hytora.http.impl.NettyHttpServer;
import lombok.var;

import java.io.File;
import java.io.PrintStream;

public class DatabaseBootstrap {

    public static void main(String[] args) {

        HandledAsyncLogger logger = new HandledAsyncLogger(LogLevel.DEBUG);
        logger.addHandler(entry -> {

            PrintStream stream = entry.getLevel() == LogLevel.ERROR ? System.err : System.out;
            stream.println(UncoloredMessageFormatter.format(entry));
        });
        Logger.setFactory(logger);

        HttpServer webServer = new NettyHttpServer();
        webServer.addListener(new HttpAddress("127.0.0.1", 8890));

        new DocumentDatabase(
                logger,
                webServer,
                new File("database/"),
                true
        );
    }
}
