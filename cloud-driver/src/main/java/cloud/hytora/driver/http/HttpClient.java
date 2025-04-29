package cloud.hytora.driver.http;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.net.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@AllArgsConstructor
public class HttpClient {




    public Response send(Request request) throws IOException, URISyntaxException {
        URI uri = request.getUri();
        if (request.post != null) {
            uri = new URI(uri.toString() + request.post);
        }
        URL url = uri.toURL();

        URLConnection uc = url.openConnection();

        uc.setUseCaches(false);
        uc.setDefaultUseCaches(false);
        for (String key : request.property.keySet()) {
            uc.addRequestProperty(key, request.property.get(key));
        }
        uc.addRequestProperty("User-Agent", "Mozilla/5.0");
        uc.addRequestProperty("Cache-Control", "no-cache, no-store, must-revalidate");
        uc.addRequestProperty("Pragma", "no-cache");

        // Parse it
        String json = new Scanner(uc.getInputStream(), "UTF-8").useDelimiter("\\A").next();

        Response response = new Response();

        response.setBody(json);

        return response;
    }


    @AllArgsConstructor
    @Getter
    public static class Request {

        private final URI uri;

        private final Duration timeOut;

        private Map<String, String> property;

        @Accessors(chain = true)
        @Setter
        private String post;


        public Request property(String key, String value){
            if (this.property == null) {
                this.property = new HashMap<>();
            }
            this.property.put(key, value);
            return this;
        }
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Response {

        private String body;
    }
}
