package cloud.hytora.modules.notify.config.sub;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MessageConfiguration {

    /**
     * The prefix that will be in front of every message
     * of this module when using the placeholder %prefix%
     */
    private String prefix;

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
        this.prefix = "§8× §bHytora§fCloud §8»";
        this.startMessage = "%prefix% §8'§b{server.node}§8' §7queued §6{server.name} §8| §3Port §b{server.port} §8| §3MaxPlayer §b{server.capacity} §8| §3Version §b{task.version} §8| §3State {server.state} §8| §3ShutdownBehaviour §b{server.type}";
        this.stopMessage = "%prefix% §8'§c{server.name}§8' §7has been stopped§8! §8| §bUptime§8: §f{server.uptime}";
        this.readyMessage = "%prefix% §8'§a{server.name}§8' §7is now ready to use§8! §8| §aBootup§8: §f{server.uptimeDifFormat} min";
    }
}
