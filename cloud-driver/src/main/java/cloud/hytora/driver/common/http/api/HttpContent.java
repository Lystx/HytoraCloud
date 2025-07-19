package cloud.hytora.driver.common.http.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter

public enum HttpContent {


    HTML_TEXT("text/html; charset=utf-8"),
    CSS_TEXT("text/css"),
    HTML_APPLICATION("application/xhtml+xml "),
    JSON("application/json"),
    ;


    private final String headerFormat;
}
