package cloud.hytora.driver.entity.services;

import cloud.hytora.document.Document;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

/**
 * The {@link ServiceCycleData} is used internally to manage timeouts of {@link CloudService}s
 * It contains the latency of packet-sending-and-receiving and also check when the last sync of the
 * specific {@link CloudService} was to determine wheter it has timed out and needs to be stopped.
 *
 *
 * The Data can also contain specific properties in form of a {@link Document} that can for example
 * contain detailed information about the current serverSoftware of a {@link CloudService}
 *  => e.g. on bukkit it contains the provided plugins of the {@link CloudService}
 *
 * @author Lystx
 * @version SNAPSHOT-1.0
 */
public interface ServiceCycleData extends IBufferObject {

    /**
     * The data of this data in form of {@link Document}
     */
    Document getData();

    /**
     * The latency (in ms) of this service
     */
    int getLatency();

    /**
     * The long date when this data was sent
     */
    long getTimestamp();
}
