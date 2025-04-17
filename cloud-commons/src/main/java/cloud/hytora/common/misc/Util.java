package cloud.hytora.common.misc;

import cloud.hytora.common.collection.NamedThreadFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Util {



    public static final String PASTE_SERVER_URL_DOCUMENTS = "https://paste.labymod.net/documents";
    public static final String PASTE_SERVER_URL = "https://paste.labymod.net/";
    public static final String PASTE_SERVER_URL_RAW = "https://paste.labymod.net/raw/";

    private static ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(4, new NamedThreadFactory("Scheduler"));


    /**
     * Public Method that tries to execute a given {@link Runnable} if a provided {@link Supplier} returns {@code true} <br>
     * or until the provided timeout in milliseconds has expired from the start of the operation
     * <br> <br>
     *
     * @param runnable the runnable to execute
     * @param request  the condition that has to be true
     * @param timeOut  the timeOut for this request in milliseconds
     */
    public static void executeIf(Runnable runnable, Supplier<Boolean> request, long timeOut) {
        scheduledExecutor.execute(() -> {
            long deadline = System.currentTimeMillis() + timeOut;
            boolean done;

            do {
                done = request.get();
                if (!done) {
                    long msRemaining = deadline - System.currentTimeMillis();
                    if (msRemaining < 0) {
                        done = true;
                    }
                } else {
                    runnable.run();
                }
            } while (!done);
        });
    }

    public static void executeIf(Runnable runnable, Supplier<Boolean> request) {
        executeIf(runnable, request, TimeUnit.DAYS.toMillis(1));
    }


    public static String uploadToHastebin(Collection<String> lines) throws IOException{
        StringBuilder s = new StringBuilder();
        for (String line : lines) {
            s.append(line).append("\n");
        }
        return uploadToHasteBin(s.toString(), false);
    }


    public static String uploadToHasteBin(String text, boolean raw) throws IOException {
        byte[] postData = text.getBytes(StandardCharsets.UTF_8);
        int postDataLength = postData.length;

        URL url = new URL(PASTE_SERVER_URL_DOCUMENTS);
        HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setInstanceFollowRedirects(false);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("User-Agent", "Hastebin Java Api");
        conn.setRequestProperty("Content-Length", Integer.toString(postDataLength));
        conn.setUseCaches(false);

        String response = null;
        DataOutputStream wr;
        try {
            wr = new DataOutputStream(conn.getOutputStream());
            wr.write(postData);
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            response = reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assert response != null;
        if (response.contains("\"key\"")) {
            response = response.substring(response.indexOf(":") + 2, response.length() - 2);

            String postURL = raw ? PASTE_SERVER_URL_RAW : PASTE_SERVER_URL;
            response = postURL + response;
        }

        return response;
    }



    public static InetSocketAddress getAddress(String address) throws Exception {
        String[] split = address.split(":");
        String hostname = Arrays.stream(Arrays.copyOfRange(split, 0, split.length - 1)).collect(Collectors.joining(":"));
        int port = Integer.parseInt(split[split.length-1]);
        return InetSocketAddress.createUnresolved(hostname, port);
    }


}
