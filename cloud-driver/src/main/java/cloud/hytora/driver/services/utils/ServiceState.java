package cloud.hytora.driver.services.utils;

import lombok.AllArgsConstructor;

import lombok.Getter;
import lombok.var;

@AllArgsConstructor
@Getter
public enum ServiceState {

    PREPARED("§6Prepared"),
    STARTING("§eStarting"),
    ONLINE("§aOnline"),
    STOPPING("§cStopping");

    private final String name;

}
