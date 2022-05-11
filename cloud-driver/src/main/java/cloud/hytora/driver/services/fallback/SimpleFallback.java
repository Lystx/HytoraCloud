package cloud.hytora.driver.services.fallback;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SimpleFallback implements FallbackEntry {

    private boolean enabled;
    private String permission;
    private int priority;

}
