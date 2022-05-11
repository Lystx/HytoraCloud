package cloud.hytora.driver.common;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.var;

@AllArgsConstructor
@Getter
public enum ConfigSplitSpacer {

    YAML(": "),
    PROPERTIES("=");
    private final String split;

}
