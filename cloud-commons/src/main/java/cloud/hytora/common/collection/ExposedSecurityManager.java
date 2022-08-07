package cloud.hytora.common.collection;

import javax.annotation.Nonnull;


public class ExposedSecurityManager extends SecurityManager {

    @Nonnull
    public Class<?>[] getPublicClassContext() {
        return getClassContext();
    }

}
