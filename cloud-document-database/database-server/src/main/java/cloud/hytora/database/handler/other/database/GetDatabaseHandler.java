package cloud.hytora.database.handler.other.database;

import cloud.hytora.common.VersionInfo;
import cloud.hytora.document.Document;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpRouter("database/findAll")
public class GetDatabaseHandler {


    @HttpEndpoint(
            method = HttpMethod.GET,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {
        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(Document.newJsonDocument())
                .setStatusCode(HttpCodes.OK)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
