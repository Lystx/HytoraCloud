import cloud.hytora.common.progressbar.ProgressBar;
import cloud.hytora.common.progressbar.ProgressBarStyle;
import cloud.hytora.common.progressbar.ProgressPrinter;
import cloud.hytora.common.task.Task;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class Test {


    public static void main(String[] args) {
        long total = 235;
        long startTime = System.currentTimeMillis();

        for (int i = 1; i <= total; i = i + 3) {
            try {
                Thread.sleep(50);
                printProgress(50 ,startTime, total, i);
            } catch (InterruptedException e) {
            }
        }
    }


    private static void printProgress(int barLength, long startTime, long total, long current) {
        long eta = current == 0 ? 0 :
                (total - current) * (System.currentTimeMillis() - startTime) / current;

        String etaHms = current == 0 ? "N/A" :
                String.format("%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(eta),
                        TimeUnit.MILLISECONDS.toMinutes(eta) % TimeUnit.HOURS.toMinutes(1),
                        TimeUnit.MILLISECONDS.toSeconds(eta) % TimeUnit.MINUTES.toSeconds(1));

        StringBuilder string = new StringBuilder(140);
        int percent = (int) (current * 100 / total);

        int add = (int) (((current - percent) * (barLength / 2)) / (barLength * 2));

        if (barLength == 100) {
            add = percent;
        }
        int borderSpaces = current == 0 ? (int) (Math.log10(add)) : (int) (Math.log10(barLength)) - (int) (Math.log10(add));


        string
                .append('\r')
                .append(String.join("", Collections.nCopies(percent == 0 ? 2 : 2 - (int) (Math.log10(percent)), " ")))
                .append(String.format(" %d%% [" + add + "] <=> [", percent))
                .append(String.join("", Collections.nCopies(add, "=")))
                .append('>')
                .append(String.join("", Collections.nCopies(borderSpaces, " ")))
                .append(']')
                .append(String.join("", Collections.nCopies(add == 0 ? 2 : (int) (Math.log10(total)) - (int) (Math.log10(current)), " ")))
                .append(String.format(" %d/%d, ETA: %s", current, total, etaHms));

        System.out.print(string);
    }

    public static Task<Void> downloadVersion(String urlStr, Path location) {
        System.out.println(" ");
        Task<Void> task = Task.empty();
        try {
            ProgressBar pb = new ProgressBar(ProgressBarStyle.COLORED_UNICODE_BLOCK, 100);


            pb.setPrinter(new ProgressPrinter() {
                @Override
                public void print(String progress) {
                    System.out.print(progress);
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
            pb.setExtraMessage("Cleaning up...");
            outputStream.close();
            inputStream.close();
            pb.close("");
            task.setResult(null);
            System.out.println(" ");
        } catch (Exception e) {
            task.setFailure(e);
        }
        return task;
    }
}
