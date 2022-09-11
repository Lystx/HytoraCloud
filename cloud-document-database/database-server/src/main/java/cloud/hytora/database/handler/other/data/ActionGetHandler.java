package cloud.hytora.database.handler.other.data;


import cloud.hytora.database.handler.DatabaseHelper;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpRouter("query/get")
public class ActionGetHandler {

    @HttpEndpoint(
            method = HttpMethod.GET,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {

        Document document = context.getRequest().getBodyAsDocument();

        Map<String, List<String>> params = context.getRequest().getQueryParameters();

        String database = params.get("database").stream().findAny().orElse(null);
        String collection = params.get("collection") == null ? null : params.get("collection").stream().findAny().orElse(null);
        String operation = params.get("operation") == null ? null : params.get("operation").stream().findAny().orElse(null);
        String identifier = params.get("identifier") == null ? null : params.get("identifier").stream().findAny().orElse(null);


        Document body = Document.newJsonDocument("message", "Unknown operation provided!");
        String finalBody = null;
        int code = HttpCodes.OK;
        if (database == null || operation == null) {
            body.set("message", "Missing Parameter: either [operation] or [database]]!");
            code = HttpCodes.BAD_REQUEST;
        } else {

            switch (operation) {

                case "identifiers": {
                    finalBody = Bundle.newJsonBundle(DatabaseHelper.getIdentifiers(database, collection)).asRawJsonString();
                    break;
                }
                case "existsCollection": {
                    finalBody = DatabaseHelper.existsCollection(database, collection).toString();
                    break;
                }
                case "listCollections": {
                    finalBody = Bundle.newJsonBundle(DatabaseHelper.listCollections(database)).asFormattedJsonString();
                    break;
                }
                case "has": {
                    finalBody = DatabaseHelper.existsEntry(database, collection, identifier).toString();
                    break;
                }
                case "findSimple": {
                    finalBody = DatabaseHelper.findEntry(database, collection, identifier).asFormattedJsonString();
                    break;
                }
            }

        }

        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(finalBody == null ? body.asFormattedJsonString() : finalBody)
                .setStatusCode(code)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
