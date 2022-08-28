package cloud.hytora.driver.node.config;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

/**
 * This interface holds values to build a {@link JavaVersion} that contains:
 *
 *  => id (e.g. 16)
 *  => name (e.g. "JAVA_16" or "Java 16" or "16_JAVA" or "My Java 16")
 *  => path (e.g. "user/java/16/bin" [whatever your path to this version might be] )
 *
 * @author Lystx
 * @since SNAPSHOT-1.4
 */
public interface JavaVersion extends IBufferObject {

    /**
     * The id of this version
     */
    int getId();

    /**
     * The name of this version
     */
    String getName();

    /**
     * The path where the bin of this version is located
     */
    String getPath();
}
