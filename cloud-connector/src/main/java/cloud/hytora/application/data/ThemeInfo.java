
package cloud.hytora.application.data;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;

@Getter
@AllArgsConstructor
public class ThemeInfo {

    public final String name;
    public final String resourceName;
    public final boolean dark;
    public final String license;
    public final String licenseFile;
    public final String sourceCodeUrl;
    public final String sourceCodePath;
    public final File themeFile;
    public final String lafClassName;

}
