package cloud.hytora.modules.notify.config;

import cloud.hytora.modules.notify.config.sub.MessageConfiguration;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class NotifyConfiguration {

    /**
     * If the module is enabled at all
     */
    private final boolean enabled;

    /**
     * If a message should appear when a service
     * is ready
     * @see MessageConfiguration
     */
    private final boolean showReadyMessage;

    /**
     * The messages for this module
     */
    private final MessageConfiguration messages;

    /**
     * A list of players {@link UUID}s that have
     * enabled receiving messages
     */
    private final Collection<UUID> enabledNotifications;

    /**
     * Default config constructor
     */
    public NotifyConfiguration() {

        this.enabled = true;
        this.showReadyMessage = true;
        this.messages = new MessageConfiguration();
        this.enabledNotifications = new ArrayList<>();
    }



}
