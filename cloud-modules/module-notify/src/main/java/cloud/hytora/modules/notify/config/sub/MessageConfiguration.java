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
        this.prefix = "§8[§bH§fC§8]";
        this.startMessage = "%prefix% §8'§b{server.node}§8' §7queued §a{server.name} §8| §bPort {server.port} §8| §bCapacity {server.capacity} §8| §bVersion {task.version} §8| §bState {server.state}";
        this.stopMessage = "%prefix% §7The Service §c{server.name} §7has been stopped§8! §8[§bUptime§8: §f{server.uptime}§8]";
        this.readyMessage = "%prefix% §7The Service §a{server.name} §7is now ready to use§8! §8[§bBootup§8: §f{server.uptimeDif}ms§8]";
    }
}
