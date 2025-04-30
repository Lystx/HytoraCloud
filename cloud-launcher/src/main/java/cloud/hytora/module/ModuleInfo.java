package cloud.hytora.module;

import cloud.hytora.Launcher;
import cloud.hytora.common.VersionInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ModuleInfo {

    private final String name;

    private final VersionInfo version;

    public String getUrl() {
        return "https://raw.github.com/Lystx/HytoraCloud/master/hytoraCloud-updater/modules/" + name + "/" + version.formatModuleJarName(name);
    }
}