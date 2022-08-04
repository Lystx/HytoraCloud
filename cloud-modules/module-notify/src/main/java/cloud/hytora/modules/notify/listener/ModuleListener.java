package cloud.hytora.modules.notify.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

public class ModuleListener {

    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ICloudServer ICloudServer = event.getICloudServer();

        this.notifyNetwork(0, ICloudServer);
    }


    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ICloudServer ICloudServer = event.getServiceInfo();
        if (ICloudServer == null) {
            return;
        }
        this.notifyNetwork(1, ICloudServer);
    }

    @EventListener
    public void handleReady(ServiceReadyEvent event) {
        ICloudServer ICloudServer = event.getCloudServer();
        if (ICloudServer == null) {
            return;
        }
        this.notifyNetwork(2, ICloudServer);
    }

    /**
     * Notifies every player on the network that has not disabled
     * receiving notification messages from this module
     *
     * @param state       the state of message (0 = start, 1 = stop, 2 = ready)
     * @param ICloudServer the server to get info about
     */
    public void notifyNetwork(int state, ICloudServer ICloudServer) {
        NotifyConfiguration config = NotifyModule.getInstance().getConfiguration();

        //if module is disabled just ignore execution
        if (!config.isEnabled()) {
            return;
        }


        String message = "";
        if (state == 2 && !config.isShowReadyMessage()) { //if state is READY but config has disabled this extra message -> ignore execution
            return;
        }

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
        message = ICloudServer.replacePlaceHolders(message);


        //iterating through all players
        for (ICloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            if (!config.getEnabledNotifications().contains(player.getUniqueId())) {
                continue; //player has disabled messages or is not empowered to receive some
            }
            PlayerExecutor executor = PlayerExecutor.forPlayer(player);

            //sending message to player
            executor.sendMessage(message.replace("%prefix%", config.getMessages().getPrefix()));
        }

        if (config.isDisplayInConsole()) {
            if (config.isDisplayPrefixInConsole()) {
                message = message.replace("%prefix%", config.getMessages().getPrefix());
            } else {
                message = message.replace("%prefix%", "");
            }
            message = message.trim();
            CloudDriver.getInstance().getLogger().info(message);
        }
    }
}
