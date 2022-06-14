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
        this.startMessage = "§8[§bNotify-Module§8] §7The Service §6{server.name} §7is now starting§8...";
        this.stopMessage = "§8[§bNotify-Module§8] §7The Service §c{server.name} §7is now stopping§8...";
        this.readyMessage = "§8[§bNotify-Module§8] §7The Service §a{server.name} §7is now ready to use§8!";
    }
}
