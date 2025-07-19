package cloud.hytora.driver.common.http.html;

import cloud.hytora.common.misc.CollectionUtils;
import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class HtmlContent {

    private String content;



    @SneakyThrows
    public static HtmlContent fromResourceFile(Class<?> accessorClass, String fileName) {

        InputStream resource = accessorClass.getResourceAsStream("/" + fileName);
        if (resource != null) {
            return new HtmlContent(FileUtils.getResourceFileAsString(resource));
        }
        return new HtmlContent("");
    }

    public static HtmlContent create(String content) {
        return new HtmlContent(content);
    }
}
