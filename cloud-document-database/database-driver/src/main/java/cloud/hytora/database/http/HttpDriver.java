package cloud.hytora.database.http;

import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;
import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.database.http.impl.HttpPayLoad;
import cloud.hytora.document.Document;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.HttpMethod;
import lombok.Getter;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.*;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class HttpDriver {

    @Getter
    private static HttpDriver instance;

    private final String token;
    private final HttpAddress address;

    public HttpDriver(String token, HttpAddress address) {
        instance = this;

        this.token = token;
        this.address = address;
    }

    public String buildBaseUrl() {
        return StringUtils.formatMessage("http://{}:{}/database/", address.getHost(), address.getPort());
    }

    public IPayLoad sendRequest(String suffix, HttpMethod methodType) {
        return this.sendRequest(suffix, methodType, p -> {}, Document.emptyDocument());
    }

    public IPayLoad sendRequest(String suffix, HttpMethod methodType, Document body) {
        return this.sendRequest(suffix, methodType, p -> {}, body);
    }

    public IPayLoad sendRequest(String suffix, HttpMethod methodType, Consumer<Map<String, String>> queryParameters) {
        return this.sendRequest(suffix, methodType, queryParameters, Document.emptyDocument());
    }

    public IPayLoad sendRequest(String suffix, HttpMethod methodType, Consumer<Map<String, String>> queryParameters, Document body) {
        String url = buildBaseUrl() + suffix;

        Map<String, String> parameters = new HashMap<>();
        queryParameters.accept(parameters);

        //Instantiate an HttpClient
        HttpClient client = new HttpClient();

        HttpMethodBase method = getBaseFromType(url, methodType, body, parameters);
        if (method == null) {
            return null;
        }

        try {
            int statusCode = client.executeMethod(method);

            IPayLoad payLoad = new HttpPayLoad(statusCode, method.getQueryString(), method.getResponseBodyAsString());
            //release connection
            method.releaseConnection();

            return payLoad;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public Task<IPayLoad> sendRequestAsync(String suffix, HttpMethod methodType, Document body) {
        return this.sendRequestAsync(suffix, methodType, p -> {}, body);
    }

    public Task<IPayLoad> sendRequestAsync(String suffix, HttpMethod methodType, Consumer<Map<String, String>> queryParameters) {
        return this.sendRequestAsync(suffix, methodType, queryParameters, Document.emptyDocument());
    }

    public Task<IPayLoad> sendRequestAsync(String suffix, HttpMethod methodType, Consumer<Map<String, String>> queryParameters, Document body) {
        Task<IPayLoad> promise = Task.empty();
        Task.runAsync(() -> {

            String url = buildBaseUrl() + suffix;

            Map<String, String> parameters = new HashMap<>();
            queryParameters.accept(parameters);

            //Instantiate an HttpClient
            HttpClient client = new HttpClient();

            HttpMethodBase method = getBaseFromType(url, methodType, body, parameters);
            if (method == null) {
                return;
            }

            try {
                int statusCode = client.executeMethod(method);

                IPayLoad payLoad = new HttpPayLoad(statusCode, method.getQueryString(), method.getResponseBodyAsString());
                //release connection
                method.releaseConnection();

                promise.setResult(payLoad);
            } catch (IOException e) {
                promise.setFailure(e);
            }
        });
        return promise;
    }


    @Nullable
    private HttpMethodBase getBaseFromType(String url, HttpMethod type, Document body, Map<String, String> queryParameters) {
        //Instantiate a GET HTTP method
        HttpMethodBase method = null;
        switch (type) {
            case GET:
                method = new GetMethod(url);
                break;
            case POST:
                method = new PostMethod(url);
                ((PostMethod)method).setRequestBody(body.asFormattedJsonString());
                break;
            case PUT:
                method = new PutMethod(url);
                break;
            case TRACE:
                method = new TraceMethod(url);
                break;
            case HEAD:
                method = new HeadMethod(url);
                break;
            case OPTIONS:
                method = new OptionsMethod(url);
                break;
            case DELETE:
                method = new DeleteMethod(url);
                break;
        }

        if (method != null) {

            Collection<NameValuePair> pairs = new ArrayList<>();

            for (Map.Entry<String, String> entry : queryParameters.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                pairs.add(new NameValuePair(key, value));
            }

            method.addRequestHeader("Authorization", this.token);
            method.setQueryString(pairs.toArray(new NameValuePair[0]));
        }
        return method;
    }
}
