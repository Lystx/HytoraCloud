package cloud.hytora.modules.sign.api.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class SignLayout {

    /**
     * The name of this animation
     */
    private final String name;

    /**
     * The lines
     */
    private final String[] lines;

    /**
     * The block behind the sign
     */
    private final String blockName;

    /**
     * The block sub-id
     */
    private final int subId;
}
