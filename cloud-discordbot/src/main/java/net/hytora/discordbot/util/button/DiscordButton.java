package net.hytora.discordbot.util.button;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.interactions.components.Button;
import net.dv8tion.jda.api.interactions.components.ButtonStyle;
import net.dv8tion.jda.internal.interactions.ButtonImpl;
import net.hytora.discordbot.Hytora;

import java.util.function.Consumer;

@AllArgsConstructor
@Getter
public class DiscordButton {

    /**
     * The ID of this button (e.g. 0x00)
     */
    private final int id;

    /**
     * The display (e.g. "Click me")
     */
    private final String display;

    /**
     * The style
     */
    private final ButtonStyle style;

    /**
     * The consumer to work with
     */
    private final Consumer<DiscordButtonAction> actionConsumer;

    /**
     * Creates the {@link Button}
     * and adds the button to the cache
     *
     * @return button
     */
    public Button submit() {
        Hytora.getHytora().getDiscordButtons().add(this);
        return new ButtonImpl(String.valueOf(id), display, style, false, null);
    }

}
