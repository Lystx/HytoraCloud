package cloud.hytora.database.api.elements;

import cloud.hytora.document.Document;

public interface DatabaseEntry extends Document {

    void setDocument(Document document);

    String getId();

    void setId(String id);
}
