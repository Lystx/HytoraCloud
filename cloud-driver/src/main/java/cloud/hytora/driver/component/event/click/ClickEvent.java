
package cloud.hytora.driver.component.event.click;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@Data
@NoArgsConstructor
public class ClickEvent implements Bufferable {

    /**
     * The action that triggers this click event
     */
    private ClickAction action;

    /**
     * The value of the click
     */
    private String value;

    /**
     * Creates a new click event with the given action and
     * value.
     *
     * @param action the action that triggered the event
     * @param value the text value
     */
    private ClickEvent(ClickAction action, String value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Creates a click action with the given action and text.
     *
     * @param action The action.
     * @param text The text.
     * @return The click action event.
     */
    public static ClickEvent of(ClickAction action, String text) {
        return new ClickEvent(action, text);
    }

    /**
     * Parses a click event from the given JSON.
     *
     * @param json The JSON.
     * @return The click event.
     */
    public static ClickEvent fromJson(Document json) {
        return of(ClickAction.valueOf(json.getString("action").toUpperCase()), json.getString("value"));
    }

    /**
     * Gets this action performance as JSON, for transmission.
     *
     * @return The JSON.
     */
    public Document asJson() {
        Document obj = DocumentFactory.newJsonDocument();
        obj.set("action", this.action.name().toLowerCase());
        obj.set("value", this.value);
        return obj;
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        if (state == BufferState.READ) {
            this.action = buf.readEnum(ClickAction.class);
            this.value = buf.readString();
        } else {
            buf.writeEnum(this.action);
            buf.writeString(this.value);
        }
    }
}
