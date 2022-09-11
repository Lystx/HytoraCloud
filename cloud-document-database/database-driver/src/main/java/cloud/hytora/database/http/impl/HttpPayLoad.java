package cloud.hytora.database.http.impl;

import cloud.hytora.database.api.IPayLoad;
import cloud.hytora.document.Bundle;
import cloud.hytora.document.Document;
import cloud.hytora.http.api.HttpCodes;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class HttpPayLoad implements IPayLoad {

    private int statusCode;
    private String queryString;
    private String response;

    @Override
    public boolean isSuccess() {
        return statusCode == HttpCodes.OK;
    }

    @Override
    public Document toDocument() {
        return Document.newJsonDocument(response);
    }

    @Override
    public Bundle toBundle() {
        return Bundle.newJsonBundle(response);
    }
}
