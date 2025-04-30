package cloud.hytora;

import cloud.hytora.common.logging.ConsoleColor;
import cloud.hytora.common.logging.LogLevel;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.common.logging.formatter.ColoredMessageFormatter;
import cloud.hytora.common.logging.handler.LogEntry;
import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.progressbar.ProgressPrinter;
import cloud.hytora.common.task.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

public class LauncherUtils {



    public static Task<Void> downloadVersion(String urlStr, Path location) {
        System.out.println("\n");
        System.out.println("\n");
        Task<Void> task = Task.empty();
        try {
            ProgressBar pb = new ProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK, 100);


            pb.setPrinter(new ProgressPrinter() {
                @Override
                public void print(String progress) {
                    System.out.print(ConsoleColor.toColoredString('§', progress));
                }
                public void flush(String progress) {
                }
            });
            pb.setAppendProgress(false);
            pb.setPrintAutomatically(true);
            pb.setExpandingAnimation(true);

            URL url = new URL(urlStr);
            String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/56.0.2924.87 Safari/537.36";
            URLConnection con = url.openConnection();
            con.setRequestProperty("User-Agent", USER_AGENT);

            int contentLength = con.getContentLength();
            InputStream inputStream = con.getInputStream();

            OutputStream outputStream = Files.newOutputStream(location);
            byte[] buffer = new byte[2048];
            int length;
            int downloaded = 0;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
                downloaded+=length;
                long step = (long) ((downloaded * 100L) / (contentLength * 1.0));
                pb.stepTo(step);
            }
            pb.setExtraMessage("Successfully downloaded! Cleaning up unused bytes...");
            outputStream.close();
            inputStream.close();
            pb.close("");
            task.setResult(null);
            System.out.println("\n");
            System.out.println("\n");
        } catch (Exception e) {
            task.setFailure(e);
        }
        return task;
    }

    public static void clearConsole() {
        try {
            if (System.getProperty("os.name").contains("Windows")) {
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            }
            else {
                System.out.print("\033\143");
            }
        } catch (IOException | InterruptedException ex) {}
    }
}
