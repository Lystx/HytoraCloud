package cloud.hytora.driver.entity.services.fallback;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class SimpleFallback implements FallbackEntry {

    private boolean enabled;
    private String permission;
    private int priority;

}
