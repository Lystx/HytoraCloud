package cloud.hytora.modules.npc.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ItemData {


    private final String material;
    private final int amount;
    private final String displayName;

}
