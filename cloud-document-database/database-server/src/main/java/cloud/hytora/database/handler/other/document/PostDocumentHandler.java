package cloud.hytora.database.handler.other.document;


import cloud.hytora.common.VersionInfo;
import cloud.hytora.document.Document;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpRouter("documents/post")
public class PostDocumentHandler {

    @HttpEndpoint(
            method = HttpMethod.POST,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {
        HttpRequest request = context.getRequest();
        String bodyString = request.getBodyString();
        Document document = Document.newJsonDocument(bodyString);

        System.out.println("Received : ");
        System.out.println(document);
        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.newJsonDocument("message", "Successfully updated DOCUMENT!"))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
