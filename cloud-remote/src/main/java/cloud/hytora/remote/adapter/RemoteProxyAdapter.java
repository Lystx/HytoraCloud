package cloud.hytora.remote.adapter;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.remote.adapter.RemoteAdapter;

import java.util.Collection;
import java.util.UUID;

public interface RemoteProxyAdapter extends RemoteAdapter {

    void registerService(ICloudServer server);

    void unregisterService(ICloudServer server);

    void clearServices();

}
