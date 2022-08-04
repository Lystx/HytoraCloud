package cloud.hytora.common.location;

public interface ModifiableLocation<N extends Number> extends ImmutableLocation<N> {

    void setX(N x);

    void setY(N y);

    void setZ(N z);

    void setWorld(String world);
}
