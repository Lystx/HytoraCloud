package cloud.hytora.remote.adapter.proxy;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.remote.adapter.RemoteAdapter;

import java.util.Collection;
import java.util.UUID;

public interface RemoteProxyAdapter extends RemoteAdapter {

    Collection<LocalProxyPlayer> getPlayers();

    default LocalProxyPlayer getPlayerByNameOrNull(String name) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getName().equalsIgnoreCase(name));
    }

    default LocalProxyPlayer getPlayerByUniqueIdOrNull(UUID uniqueId) {
        return DriverUtility.findOrNull(getPlayers(), p -> p.getUniqueId().equals(uniqueId));
    }

    void registerService(ICloudService server);

    void unregisterService(ICloudService server);

    void clearServices();

}
