package cloud.hytora.database.handler.other.data;


import cloud.hytora.common.logging.Logger;
import cloud.hytora.database.handler.DatabaseHelper;
import cloud.hytora.document.Document;
import cloud.hytora.http.api.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

@HttpRouter("query/post")
public class ActionPostHandler {

    @HttpEndpoint(
            method = HttpMethod.POST,
            permission = "cloud.database.access"
    )
    public void handle(@Nonnull HttpContext context) {
        Logger.constantInstance().debug("Handling POST-REQUEST from {}", context.getChannel().getClientAddress());

        Document document = context.getRequest().getBodyAsDocument();

        Map<String, List<String>> params = context.getRequest().getQueryParameters();

        String database = params.get("database").stream().findAny().orElse(null);
        String collection = params.get("collection") == null ? null : params.get("collection").stream().findAny().orElse(null);
        String operation = params.get("operation") == null ? null : params.get("operation").stream().findAny().orElse(null);
        String identifier = params.get("identifier") == null ? null : params.get("identifier").stream().findAny().orElse(null);

        if (identifier == null || identifier.trim().isEmpty() && document.has("_id")) {
            identifier = document.removeAndGet("_id").toString();
        }

        Document body = Document.newJsonDocument("message", "Unknown operation provided!");
        int code = HttpCodes.OK;
        if (database == null || collection == null || operation == null) {
            body.set("message", "Missing Parameter: either [operation] [database] or [collection]!");
            code = HttpCodes.BAD_REQUEST;
        } else {
            switch (operation) {


                case "insert": {
                    Logger.constantInstance().debug("Insert request from {} with id '{}'", context.getChannel().getClientAddress(), identifier);
                    if (DatabaseHelper.existsEntry(database, collection, identifier)) {
                        Logger.constantInstance().debug("  => Entry with id '{}' already exists!", identifier);
                        code = HttpCodes.BAD_REQUEST;
                        body.set("message", "Entry with id '" + identifier + "' already exists! Use update method!");
                    } else {
                        DatabaseHelper.upsertEntry(database, collection, document, identifier);
                        body.set("message", "Successfully inserted entry with id '" + identifier + "'");
                        Logger.constantInstance().debug("  => Entry with id '{}' was successfully inserted!", identifier);
                    }
                    break;
                }
                case "createCollection": {

                    Logger.constantInstance().debug("CreateCollection request from {} with name '{}' in DB {}", context.getChannel().getClientAddress(), collection, database);
                    if (DatabaseHelper.existsCollection(database, collection)) {
                        Logger.constantInstance().debug("  => Collection with name '{}' already exists!", collection);
                        code = HttpCodes.BAD_REQUEST;
                        body.set("message", "Collection with name '" + collection + "' already exists!");
                    } else {
                        DatabaseHelper.createCollection(database, collection);
                        body.set("message", "Successfully created collection with name '" + collection + "' in DB " + database);
                        Logger.constantInstance().debug("  => Collection with name '{}' in DB {} was successfully inserted!", collection, database);

                    }
                    break;
                }

                case "delete": {
                    Logger.constantInstance().debug("Delete request from {} with id '{}'", context.getChannel().getClientAddress(), identifier);

                    if (!DatabaseHelper.existsEntry(database, collection, identifier)) {
                        Logger.constantInstance().debug("  => Entry with id '{}' does not exists!", identifier);

                        code = HttpCodes.BAD_REQUEST;
                        body.set("message", "Entry with id '" + identifier + "' does not exists!!");
                    } else {
                        DatabaseHelper.deleteEntry(database, collection, identifier);
                        body.set("message", "Successfully deleted entry with id '" + identifier + "'");
                        Logger.constantInstance().debug("  => Entry with id '{}' was successfully deleted!", identifier);

                    }
                    break;
                }
                case "drop": {

                    if (!DatabaseHelper.existsCollection(database, collection)) {
                        code = HttpCodes.BAD_REQUEST;
                        body.set("message", "Collection with name '" + collection + "' does not exists in database '" + database + "'!");
                    } else {
                        DatabaseHelper.deleteCollection(database, collection);
                        body.set("message", "Successfully deleted collection with name'" + collection + "'");
                    }
                    break;
                }
                case "update": {
                    Logger.constantInstance().debug("Update request from {} with id '{}'", context.getChannel().getClientAddress(), identifier);

                    if (!DatabaseHelper.existsEntry(database, collection, identifier)) {
                        Logger.constantInstance().debug("  => Entry with id '{}' does not exists!", identifier);
                        code = HttpCodes.BAD_REQUEST;
                        body.set("message", "Entry with id '" + identifier + "' does not exists! Use insert method!");
                    } else {
                        DatabaseHelper.upsertEntry(database, collection, document, identifier);
                        body.set("message", "Successfully updated entry with id '" + identifier + "'");
                        Logger.constantInstance().debug("  => Entry with id '{}' was successfully updated!", identifier);

                    }
                    break;
                }
            }

        }

        context.getResponse()
                .setHeader("Content-Type", "application/json")
                .setBody(body.asFormattedJsonString())
                .setStatusCode(code)
                .getContext()
                .closeAfter(true)
                .cancelNext(true);
    }
}
