package cloud.hytora.database.handler.other.document;


import cloud.hytora.common.VersionInfo;
import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpRouter("documents/get")
public class GetDocumentHandler {

    @HttpEndpoint(
            method = HttpMethod.GET,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {
        Map<String, List<String>> queryParameters = context.getRequest().getQueryParameters();
        System.out.println(queryParameters);
        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.newJsonDocument(VersionInfo.getCurrentVersion()))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
