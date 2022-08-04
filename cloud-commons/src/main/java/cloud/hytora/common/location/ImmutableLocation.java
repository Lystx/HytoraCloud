package cloud.hytora.common.location;

public interface ImmutableLocation<N extends Number> {

    N getX();

    N getY();

    N getZ();

    String getWorld();

}
