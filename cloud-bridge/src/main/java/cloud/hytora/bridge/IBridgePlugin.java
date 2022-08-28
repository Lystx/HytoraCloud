package cloud.hytora.bridge;

import cloud.hytora.document.Bundle;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.commands.context.defaults.PlayerCommandContext;
import cloud.hytora.driver.commands.data.DriverCommand;
import cloud.hytora.driver.commands.data.ProtocolCommandInfo;
import cloud.hytora.driver.commands.data.enums.CommandScope;
import cloud.hytora.driver.common.CloudMessages;
import cloud.hytora.driver.player.ICloudPlayer;
import cloud.hytora.driver.player.ICloudPlayerManager;
import cloud.hytora.driver.player.executor.PlayerExecutor;
import cloud.hytora.driver.player.packet.CloudPlayerExecuteCommandPacket;
import cloud.hytora.driver.services.ICloudServiceManager;
import cloud.hytora.driver.services.utils.RemoteIdentity;
import cloud.hytora.driver.services.utils.ServiceState;
import cloud.hytora.driver.services.utils.ServiceVisibility;
import cloud.hytora.driver.storage.INetworkDocumentStorage;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public interface IBridgePlugin {

    default void bootstrap() {
        //updating service
        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ICloudServiceManager.class)
                .thisService()
                .onTaskSucess(cloudServer -> {
                    cloudServer.setServiceVisibility(ServiceVisibility.VISIBLE);
                    cloudServer.setServiceState(ServiceState.ONLINE);
                    cloudServer.setReady(true);
                    cloudServer.update();
                    CloudDriver.getInstance().getLogger().info("Service = CloudServer[name={}, port={}, state={}, visibility={}]", cloudServer.getName(), cloudServer.getPort(), cloudServer.getServiceState(), cloudServer.getServiceVisibility());
                }).onTaskFailed(e -> {
                    System.out.println("HUGE MISTAKE");
                });
    }

    default RemoteIdentity getIdentity() {
        return RemoteIdentity.read(new File("property.json"));
    }

    default void handleCommandExecution(UUID playerUUID, String commandLine, Consumer<Boolean> cancelled) {
        ICloudPlayer cloudPlayer = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudPlayerManager.class).getCloudPlayerByUniqueIdOrNull(playerUUID);
        PlayerExecutor executor = PlayerExecutor.forPlayer(cloudPlayer);
        if (cloudPlayer == null) {
            executor.disconnect(CloudMessages.retrieveFromStorage().getPrefix() + " §cCouldn't find your CloudPlayer. Please rejoin!");
            return;
        }


        INetworkDocumentStorage storage = CloudDriver.getInstance().getProviderRegistry().getUnchecked(INetworkDocumentStorage.class);
        Bundle bundle = storage.getBundle("ingameCommands");
        List<ProtocolCommandInfo> commands = bundle.toList(ProtocolCommandInfo.class);


        String finalCommandLine = commandLine;
        finalCommandLine = finalCommandLine.replaceFirst("/", "");
        String finalCommandLine1 = finalCommandLine;
        ProtocolCommandInfo protocolCommandInfo = commands.stream().filter(c -> finalCommandLine1.startsWith(c.getPath())).findFirst().orElse(null);
        if (protocolCommandInfo != null) {
            if (commandLine.startsWith("cloud ")) {
                commandLine = commandLine.replace("cloud ", "");
            }
            //commandLine = commandLine.replace(commandObject.getPath(), "");
            cancelled.accept(true);
            if (protocolCommandInfo.getScope() == CommandScope.INGAME) {
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).executeCommand(commandLine.split(" "), new PlayerCommandContext(cloudPlayer));
            } else if (protocolCommandInfo.getScope() == CommandScope.CONSOLE_AND_INGAME | protocolCommandInfo.getScope() == CommandScope.INGAME_HOSTED_ON_CLOUD_SIDE) {

                CloudPlayerExecuteCommandPacket packet = new CloudPlayerExecuteCommandPacket(cloudPlayer.getUniqueId(), commandLine);
                packet.publishAsync(); //executing packet to send to cloud
            }
        } else {
            String command = commandLine.substring(1).split(" ")[0];
            DriverCommand registeredCommand = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).getRootCommands().stream().filter(c -> c.getNames().stream().anyMatch(s -> s.equalsIgnoreCase(command))).findFirst().orElse(null);
            if (registeredCommand != null) {
                cancelled.accept(true);
                CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICommandManager.class).executeCommand(commandLine.split(" "), new PlayerCommandContext(cloudPlayer));
            }
        }

    }

    void shutdown();

}
