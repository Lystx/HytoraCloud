
package cloud.hytora.driver.component.event.hover;

import cloud.hytora.document.Document;
import cloud.hytora.document.DocumentFactory;
import cloud.hytora.document.JsonEntity;
import cloud.hytora.driver.component.ChatComponent;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;


@Data
@NoArgsConstructor
public class HoverEvent implements Bufferable {
    /**
     * The action that triggered this hover event
     */
    private HoverAction action;

    /**
     * The text that triggers this event
     */
    private JsonEntity value;

    /**
     * Creates a new hover event with the given action
     * and the given text that triggered it.
     *
     * @param action the triggering action
     * @param value the triggering text value
     */
    public HoverEvent(HoverAction action, JsonEntity value) {
        this.action = action;
        this.value = value;
    }

    /**
     * Creates a show text hover action with text.
     *
     * @param text The text.
     * @return The hover action event.
     */
    public static HoverEvent text(String text) {
        return text(ChatComponent.text(text));
    }

    /**
     * Creates a show text hover action with a chat
     * component.
     *
     * @param chat The chat component.
     * @return The hover action event.
     */
    public static HoverEvent text(ChatComponent chat) {
        return new HoverEvent(HoverAction.SHOW_TEXT, chat.toDocument());
    }

    /**
     * Creates a show achievement hover action with an
     * achievement.
     *
     * @param achievement The achievement.
     * @return The hover action event.
     */
    public static HoverEvent achievement(String achievement) {
        return new HoverEvent(HoverAction.SHOW_ACHIEVEMENT, DocumentFactory.newJsonDocument(achievement));
    }

    /**
     * Adds an item property to the chat component.
     *
     * @param id the id as string of the item
     * @param count the amount of the item
     * @param damage the damage to the item
     * @return the new hover action event
     */
    public static HoverEvent item(String id, int damage, int count) {
        Document json = DocumentFactory.newJsonDocument();
        json.set("id", id);
        json.set("Damage", damage);
        json.set("Count", count);
        json.set("tag", "{}");

        return new HoverEvent(HoverAction.SHOW_ITEM, json);
    }

    /**
     * Parses a click event from the given JSON.
     *
     * @param json The JSON.
     * @return The click event.
     */
    public static HoverEvent fromJson(Document json) {
        return new HoverEvent(HoverAction.valueOf(json.getString("action").toUpperCase()), json.getEntry("value"));
    }

    /**
     * Gets this action performance as JSON, for
     * transmission.
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
            this.action = buf.readEnum(HoverAction.class);
            this.value = null; // TODO: 25.07.2022
        } else {
            buf.writeEnum(action);
            // TODO: 25.07.2022 write entity
        }
    }
}
