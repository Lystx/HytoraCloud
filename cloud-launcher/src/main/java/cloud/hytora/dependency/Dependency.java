package cloud.hytora.dependency;


import java.nio.file.Path;
import java.nio.file.Paths;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@Getter
@AllArgsConstructor
public class Dependency {

    private final String group;
    private final String name;
    private final String version;

    private final String repository;

    public Path toPath() {
        String fileName = String.format("%s-%s%s.jar", this.name, this.version, "");

        return Paths.get(this.group.replace(".", "/"), this.name, this.version, fileName);
    }

}

