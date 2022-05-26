package cloud.hytora.driver.property;

import cloud.hytora.document.Document;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public interface PropertyHolder {

    @NotNull
    Document getProperties();

    void setProperties(@NotNull Document properties);

    @Nullable
    <T> T getProperty(@NotNull String name, @NotNull Class<T> typeClass);

    void setProperty(@NotNull String name, @Nullable Object value);

}
