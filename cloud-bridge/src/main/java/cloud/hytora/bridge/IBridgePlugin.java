package cloud.hytora.bridge;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.ProtocolCommandInfo;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.common.UniqueReturnValue;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.services.ICloudServer;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.storage.INetworkDocumentStorage;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * An interface to define plugins (bungee, velocity, spigot etc)
 * With override methods that every plugin instance needs
 *
 * @author Lystx
 * @since SNAPSHOT-1.3
 */
public interface IBridgePlugin {

    /**
     * Boots up this plugin internally and sets the state of the {@link ICloudServer}
     * to online and syncs it with the node
     */
    default void bootstrap() {
        //accessing current server
        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .thisService()
                .onTaskSucess(cloudServer -> {

                    //setting new service values
                    cloudServer.setServiceVisibility(ServiceVisibility.VISIBLE);
                    cloudServer.setServiceState(ServiceState.ONLINE);
                    cloudServer.setReady(true);

                    //updating service
                    cloudServer.update();

                    //logging success to console
                    CloudDriver.getInstance().getLogger().info("Service = CloudServer[name={}, port={}, state={}, visibility={}]", cloudServer.getName(), cloudServer.getPort(), cloudServer.getServiceState(), cloudServer.getServiceVisibility());
                }).onTaskFailed(e -> {
            //something went wrong and server could not be recognised
            System.out.println("HUGE MISTAKE");
        });
    }

    /**
     * Reads the passed on {@link RemoteIdentity} that was set whilst
     * preparing this server on the node-side
     *
     * @return newly read instance
     */
    @UniqueReturnValue
    @NotNull
    default RemoteIdentity getIdentity() {
        return RemoteIdentity.read(new File("property.json"));
    }

    /**
     * Default method to handle the command execution of a player
     *
     * @param playerUUID  the uuid of the player
     * @param commandLine the plain line that was executed
     * @param cancelled   the handler if the event was cancelled
     */
    default void handleCommandExecution(final UUID playerUUID, final String commandLine, final Consumer<Boolean> cancelled) {
        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudPlayerManager.class)
                .getCloudPlayer(playerUUID)
                .ifPresentOrElse(cloudPlayer -> {

                    INetworkDocumentStorage storage = CloudDriver.getInstance()
                            .getProviderRegistry()
                            .getUnchecked(INetworkDocumentStorage.class);
                    Bundle bundle = storage.getBundle("ingameCommands");
                    List<ProtocolCommandInfo> commands = bundle.toList(ProtocolCommandInfo.class);

                    String finalCommandLine = commandLine;
                    finalCommandLine = finalCommandLine.replaceFirst("/", "");
                    String finalCommandLine1 = finalCommandLine;
                    ProtocolCommandInfo protocolCommandInfo = commands.stream().filter(c -> finalCommandLine1.startsWith(c.getPath())).findFirst().orElse(null);
                    if (protocolCommandInfo != null) {
                        if (finalCommandLine.startsWith("cloud ")) {
                            finalCommandLine = finalCommandLine.replace("cloud ", "");
                        }
                        //finalCommandLine = finalCommandLine.replace(commandObject.getPath(), "");
                        cancelled.accept(true);
                        if (protocolCommandInfo.getScope() == CommandScope.INGAME) {
                            CloudDriver.getInstance()
                                    .getProviderRegistry()
                                    .getUnchecked(ICommandManager.class)
                                    .executeCommand(
                                            finalCommandLine.split(" "),
                                            new PlayerCommandContext(cloudPlayer)
                                    );
                        } else if (protocolCommandInfo.getScope() == CommandScope.CONSOLE_AND_INGAME | protocolCommandInfo.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {

                            CloudPlayerExecuteCommandPacket packet = new CloudPlayerExecuteCommandPacket(
                                    cloudPlayer.getUniqueId(),
                                    finalCommandLine);
                            packet.publishAsync(); //executing packet to send to cloud
                        }
                    } else {
                        String command = finalCommandLine.substring(1).split(" ")[0];
                        DriverCommand registeredCommand = CloudDriver.getInstance()
                                .getProviderRegistry()
                                .getUnchecked(ICommandManager.class)
                                .getRootCommands()
                                .stream().
                                        filter(c -> c.getNames()
                                                .stream()
                                                .anyMatch(s -> s.equalsIgnoreCase(command)))
                                .findFirst()
                                .orElse(null);
                        if (registeredCommand != null) {
                            cancelled.accept(true);
                            CloudDriver.getInstance()
                                    .getProviderRegistry()
                                    .getUnchecked(ICommandManager.class)
                                    .executeCommand(
                                            finalCommandLine.split(" "),
                                            new PlayerCommandContext(cloudPlayer)
                                    );
                        }
                    }

                }, () -> {
                    PlayerExecutor executor = PlayerExecutor.forUniqueId(playerUUID);
                    executor.disconnect(
                            CloudMessages.retrieveFromStorage().getPrefix()
                                    + " §cCouldn't find your CloudPlayer. Please rejoin!"
                    );
                });
    }

    /**
     * Method to shut down this plugin
     * Should implement the shutdown of the underlying plugin instance and server shutdown
     */
    void shutdown();

}
