package cloud.hytora.driver.common;

import cloud.hytora.document.Document;

public interface Documentable<T> {

    Document toDocument();

    void applyDocument(Document document);
}
