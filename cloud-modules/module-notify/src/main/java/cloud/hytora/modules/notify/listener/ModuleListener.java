package cloud.hytora.modules.notify.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.player.CloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ServiceInfo;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

public class ModuleListener {


    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ServiceInfo serviceInfo = event.getServiceInfo();

        this.notifyNetwork(0, serviceInfo);
    }


    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ServiceInfo serviceInfo = event.getServiceInfo();
        if (serviceInfo == null) {
            return;
        }
        this.notifyNetwork(1, serviceInfo);
    }

    @EventListener
    public void handleReady(ServiceReadyEvent event) {
        ServiceInfo serviceInfo = event.getServiceInfo();
        if (serviceInfo == null) {
            return;
        }
        this.notifyNetwork(2, serviceInfo);
    }

    /**
     * Notifies every player on the network that has not disabled
     * receiving notification messages from this module
     *
     * @param state       the state of message (0 = start, 1 = stop, 2 = ready)
     * @param serviceInfo the server to get info about
     */
    public void notifyNetwork(int state, ServiceInfo serviceInfo) {
        NotifyConfiguration config = NotifyModule.getInstance().getConfiguration();

        //if module is disabled just ignore execution
        if (!config.isEnabled()) {
            return;
        }

        //iterating through all players
        for (CloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            if (config.getDisabledMessages().contains(player.getUniqueId())) {
                continue; //player has disabled messages
            }
            PlayerExecutor executor = PlayerExecutor.forPlayer(player);
            if (state == 2 && !config.isShowReadyMessage()) { //if state is READY but config has disabled this extra message -> ignore execution
                return;
            }

            String message = "";
            switch (state) {
                case 0: //starting of server
                    message = config.getMessages().getStartMessage();
                    break;
                case 1: //stopping of server
                    message = config.getMessages().getStopMessage();
                    break;

                case 2: //ready of server
                    message = config.getMessages().getReadyMessage();
                    break;
            }

            //applying placeholders
            serviceInfo.replacePlaceHolders(message);

            //sending message to player
            executor.sendMessage(message);
        }
    }
}
