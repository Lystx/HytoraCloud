package cloud.hytora.modules.notify.listener;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.event.EventListener;
import cloud.hytora.driver.event.defaults.server.ServiceReadyEvent;
import cloud.hytora.driver.event.defaults.server.ServiceRegisterEvent;
import cloud.hytora.driver.event.defaults.server.ServiceUnregisterEvent;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.services.ICloudService;
import cloud.hytora.modules.notify.NotifyModule;
import cloud.hytora.modules.notify.NotifyState;
import cloud.hytora.modules.notify.config.NotifyConfiguration;

public class ModuleListener {

    @EventListener
    public void handleAdd(ServiceRegisterEvent event) {
        ICloudService cloudServer = event.getCloudServer();

        this.notifyNetwork(NotifyState.START, cloudServer);
    }


    @EventListener
    public void handleRemove(ServiceUnregisterEvent event) {
        ICloudService cloudServer = event.getCloudServer();
        if (cloudServer == null) {
            return;
        }
        this.notifyNetwork(NotifyState.STOP, cloudServer);
    }

    @EventListener
    public void handleReady(ServiceReadyEvent event) {
        ICloudService cloudServer = event.getCloudServer();
        if (cloudServer == null) {
            return;
        }
        this.notifyNetwork(NotifyState.READY, cloudServer);
    }

    /**
     * Notifies every player on the network that has not disabled
     * receiving notification messages from this module
     *
     * @param state       the state of message (0 = start, 1 = stop, 2 = ready)
     * @param cloudService the server to get info about
     */
    public void notifyNetwork(NotifyState state, ICloudService cloudService) {
        NotifyConfiguration config = NotifyModule.getInstance().getConfiguration();

        //if module is disabled just ignore execution
        if (!config.isEnabled()) {
            return;
        }


        String message = "";
        if (state == NotifyState.READY && !config.isShowReadyMessage()) { //if state is READY but config has disabled this extra message -> ignore execution
            return;
        }

        switch (state) {
            case START: //starting of server
                message = config.getMessages().getStartMessage();
                break;
            case STOP: //stopping of server
                message = config.getMessages().getStopMessage();
                break;

            case READY: //ready of server
                message = config.getMessages().getReadyMessage();
                break;
        }

        //applying placeholders
        message = cloudService.replacePlaceHolders(message);


        //iterating through all players
        for (ICloudPlayer player : CloudDriver.getInstance().getPlayerManager().getAllCachedCloudPlayers()) {
            if (!config.getEnabledNotifications().contains(player.getUniqueId())) {
                continue; //player has disabled messages or is not empowered to receive some
            }
            if (!player.hasPermission("cloud.modules.notify.command.use")) {
                continue;
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
