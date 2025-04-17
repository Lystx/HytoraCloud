package cloud.hytora.modules.cloud;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;
import java.util.Map;

@AllArgsConstructor
@Getter
@Setter
public class PermissionData {


    private Map<String, Long> permissions;

    private Collection<String> deniedPermissions;

    private Map<String, Collection<String>> taskPermissions;
}
