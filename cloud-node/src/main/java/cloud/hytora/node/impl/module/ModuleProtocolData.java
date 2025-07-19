package cloud.hytora.node.impl.module;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.File;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ModuleProtocolData {

    private File jarFile;
    private File folder;
}
