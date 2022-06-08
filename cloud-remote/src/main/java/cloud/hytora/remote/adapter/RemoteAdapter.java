package cloud.hytora.remote.adapter;

import cloud.hytora.common.DriverUtility;

public interface RemoteAdapter {

    void executeCommand(String command);

    void shutdown();
}
