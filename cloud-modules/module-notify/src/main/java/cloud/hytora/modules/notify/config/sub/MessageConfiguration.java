package cloud.hytora.modules.notify.config.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageConfiguration {

    /**
     * The message when a service is starting
     */
    private final String startMessage;

    /**
     * The message when a service is stopping
     */
    private final String stopMessage;

    /**
     * The message when a service is ready
     */
    private final String readyMessage;

    /**
     * Default config constructor
     */
    public MessageConfiguration() {
        this.startMessage = "%prefix% §8'%1{server.node}§8' §7queued %2{server.name} §8| %2Port %1{server.port} §8| %2MaxPlayer %1{server.capacity} §8| %2Version %1{task.version} §8| %2State {server.state} §8| %2ShutdownBehaviour %1{server.type}";
        this.stopMessage = "%prefix% §8'§c{server.name}§8' §7has been stopped§8! §8| %1Uptime§8: §f{server.uptime}";
        this.readyMessage = "%prefix% §8'§a{server.name}§8' §7is now ready to use§8! §8| §aBootup§8: §f{server.uptimeDifFormat} min";
    }
}
