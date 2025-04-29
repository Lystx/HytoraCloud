package cloud.hytora.common.location.impl;

import cloud.hytora.common.location.ModifiableLocation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CloudEntityLocation<N extends Number, F extends Number> implements ModifiableLocation<N> {

    protected N x;
    protected N y;
    protected N z;

    protected F yaw;
    protected F pitch;

    protected String world;


    public static <N extends Number, F extends Number> CloudEntityLocation<N, F> get(N x, N y, N z) {
        CloudEntityLocation<N, F> location = new CloudEntityLocation<>();
        location.setX(x);
        location.setY(y);
        location.setZ(z);
        return location;
    }


    public static <N extends Number, F extends Number> CloudEntityLocation<N, F> get(N x, N y, N z, F yaw, F pitch) {
        CloudEntityLocation<N, F> location = get(x, y, z);

        location.setYaw(yaw);
        location.setPitch(pitch);

        return location;
    }
}
