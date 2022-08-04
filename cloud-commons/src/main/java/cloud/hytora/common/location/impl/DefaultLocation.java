package cloud.hytora.common.location.impl;

import cloud.hytora.common.location.ModifiableLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class DefaultLocation<N extends Number> implements ModifiableLocation<N> {

    private N x;
    private N y;
    private N z;
    private String world;

}
