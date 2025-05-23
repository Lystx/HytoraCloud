package cloud.hytora.modules.sign.api;

import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;

public enum SignState {

    OFFLINE,

    STARTING,

    ONLINE,

    FULL,

    MAINTENANCE;


    public static SignState ofServer(ICloudService server) {
        if (server.getTask().isMaintenance()) {
            return MAINTENANCE;
        } else if (server.getOnlinePlayerCount() >= server.getMaxPlayers()) {
            return FULL;
        } else if (server.getServiceState() == ServiceState.ONLINE) {
            return ONLINE;
        } else if (server.getServiceState() == ServiceState.PREPARED || server.getServiceState() == ServiceState.STARTING) {
            return STARTING;
        } else if (server.getServiceVisibility() == ServiceVisibility.INVISIBLE) {
            return OFFLINE;
        } else {
            return OFFLINE;
        }
    }
}
