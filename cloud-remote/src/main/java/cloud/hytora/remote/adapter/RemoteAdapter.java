package cloud.hytora.remote.adapter;

import cloud.hytora.common.DriverUtility;
import cloud.hytora.driver.services.IServiceCycleData;

public interface RemoteAdapter {

    void executeCommand(String command);

    IServiceCycleData createCycleData();

    void shutdown();
}
