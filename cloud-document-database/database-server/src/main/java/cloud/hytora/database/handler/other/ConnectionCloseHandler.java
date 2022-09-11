package cloud.hytora.database.handler.other;


import cloud.hytora.common.logging.Logger;
import cloud.hytora.document.Document;
import cloud.hytora.http.HttpAddress;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;

@HttpRouter("connection/close")
public class ConnectionCloseHandler {

    @HttpEndpoint(
            method = HttpMethod.POST,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {
        HttpRequest request = context.getRequest();
        String bodyString = request.getBodyString();
        Document document = Document.newJsonDocument(bodyString);

        String user = document.get("name").toString();
        HttpAddress address = HttpAddress.fromString(document.get("address").toString());
        Logger.constantInstance().info("User {} disconnected from {}", user, address.toString());

        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.newJsonDocument("message", "Successfully closed connection!"))
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
