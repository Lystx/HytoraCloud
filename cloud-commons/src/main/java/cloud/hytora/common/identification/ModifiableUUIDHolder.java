package cloud.hytora.common.identification;

import java.util.UUID;

public interface ModifiableUUIDHolder extends ImmutableUUIDHolder {

    void setUniqueId(UUID uniqueId);
}
