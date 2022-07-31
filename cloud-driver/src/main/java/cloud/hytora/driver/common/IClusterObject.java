package cloud.hytora.driver.common;

import cloud.hytora.driver.networking.IPacketExecutor;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

public interface IClusterObject<T> extends IPacketExecutor, ICloneableObject<T>, IdentityObject, IPlaceHolderObject, IBufferObject {



    String getName();
}
