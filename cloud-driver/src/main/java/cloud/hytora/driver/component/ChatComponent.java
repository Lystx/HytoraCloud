
package cloud.hytora.driver.component;

import cloud.hytora.document.*;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverEvent;
import cloud.hytora.driver.networking.protocol.codec.buf.Bufferable;
import cloud.hytora.driver.networking.protocol.codec.buf.PacketBuffer;
import cloud.hytora.driver.networking.protocol.packets.BufferState;
import lombok.*;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Represents a component in a Minecraft chat format.
 *
 * <p>This class is <strong>NOT</strong> thread-safe!</p>
 *
 * @author Lystx
 * @since 0.5-alpha
 */
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ChatComponent implements Bufferable {
    /**
     * The text that this chat component represents
     */
    @Getter @Setter
    private String text;
    /**
     * The translate modifier for the chat message
     */
    @Getter @Setter
    private String translate;
    /**
     * The chat selector
     */
    @Getter @Setter
    private String selector;
    /**
     * The insertion modifier for the chat message
     */
    @Getter @Setter
    private String insertion;
    /**
     * The scoreboard username
     */
    @Getter @Setter
    private String scoreUsername;
    /**
     * The scoreboard objective
     */
    @Getter @Setter
    private String scoreObjective;

    /**
     * The color of the chat message
     */
    @Getter @Setter
    private ChatColor color;
    /**
     * A click event that this message may contain
     */
    @Getter @Setter
    private ClickEvent clickEvent;
    /**
     * A hover event that this message may contain
     */
    @Getter @Setter
    private HoverEvent hoverEvent;

    /**
     * The list of chat components added to the 'with' array.
     */
    private final List<ChatComponent> with = new ArrayList<>();

    /**
     * The list of chat components added to the 'extra' array.
     */
    private final List<ChatComponent> extra = new ArrayList<>();

    /**
     * Whether or not this message is bolded
     */
    private Boolean bold;

    /**
     * Whether or not this message is italicized
     */
    private Boolean italic;

    /**
     * Whether or not this message is underlined
     */
    private Boolean underlined;

    /**
     * Whether or not this message is crossed out
     */
    private Boolean strikethrough;

    /**
     * Whether or not this message is obfuscated
     */
    private Boolean obfuscated;

    /**
     * Gets all elements attached to the 'with' array.
     *
     * The 'with' array is used in conjunction with the 'translate' component.
     *
     * @return The with elements.
     */
    public List<ChatComponent> getWith() {
        return this.with;
    }

    /**
     * Adds a component to the 'with' array.
     *
     * @param component The component.
     * @return This object.
     */
    public ChatComponent addWith(ChatComponent component) {
        if (!this.hasWith(component, true)) {
            this.with.add(component);
        }
        return this;
    }

    /**
     * Adds a string to the 'with' array.
     *
     * @param string The string.
     * @return This object.
     */
    public ChatComponent addWith(String string) {
        return this.addWith(new StringChatComponent(string));
    }

    /**
     * Checks if this component contains the given
     * component in the 'with' array, optionally recursively.
     *
     * @param component The component
     * @param recursive Whether to check children as well.
     * @return True iff the given component exists in this
     * component's hierarchy.
     */
    public boolean hasWith(ChatComponent component, boolean recursive) {
        List<ChatComponent> with = this.with;
        if (component == this || with.contains(component)) {
            return true;
        } else if (!recursive) {
            return false;
        }

        for (ChatComponent child : with) {
            if (child.hasWith(component, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all components attached to the 'extra' array.
     *
     * <p>Components in this array are seted to this
     * component.</p>
     *
     * @return The extra components.
     */
    public List<ChatComponent> getExtra() {
        return this.extra;
    }

    /**
     * Adds a component to the 'extra' array.
     *
     * @param component The component.
     * @return This component.
     */
    public ChatComponent append(ChatComponent component) {
        if (!this.hasExtra(component, true)) {
            this.extra.add(component);
        }
        return this;
    }


    /**
     * Checks if this component contains the given
     * component in the 'extra' array, optionally recursively.
     *
     * @param component The component
     * @param recursive Whether to check children as well.
     * @return True iff the given component exists in this
     * component's hierarchy.
     */
    public boolean hasExtra(ChatComponent component, boolean recursive) {
        List<ChatComponent> extra = this.extra;
        if (component == this || extra.contains(component)) {
            return true;
        } else if (!recursive) {
            return false;
        }

        for (ChatComponent child : extra) {
            if (child.hasExtra(component, true)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets whether this component is bold.
     *
     * @return True iff it is.
     */
    public boolean isBold() {
        Boolean flag = this.bold;
        return flag != null && flag;
    }

    /**
     * Sets this component's boldness.
     *
     * @param bold The boldness.
     * @return This component.
     */
    public ChatComponent setBold(boolean bold) {
        this.bold = bold;
        return this;
    }

    /**
     * Gets whether this component is in italics.
     *
     * @return True iff it is.
     */
    public boolean isItalic() {
        Boolean flag = this.italic;
        return flag != null && flag;
    }

    /**
     * Sets this component's italic.
     *
     * @param italic The italic.
     * @return This component.
     */
    public ChatComponent setItalic(boolean italic) {
        this.italic = italic;
        return this;
    }

    /**
     * Gets whether this component is underlined.
     *
     * @return True iff it is.
     */
    public boolean isUnderlined() {
        Boolean flag = this.underlined;
        return flag != null && flag;
    }

    /**
     * Sets this component's underlined.
     *
     * @param underlined The underline.
     * @return This component.
     */
    public ChatComponent setUnderlined(boolean underlined) {
        this.underlined = underlined;
        return this;
    }

    /**
     * Gets whether this component is striked through.
     *
     * @return True iff it is.
     */
    public boolean isStrikethrough() {
        Boolean flag = this.strikethrough;
        return flag != null && flag;
    }

    /**
     * Sets this component's strikethrough.
     *
     * @param strikethrough The strikethrough.
     * @return This component.
     */
    public ChatComponent setStrikethrough(boolean strikethrough) {
        this.strikethrough = strikethrough;
        return this;
    }

    /**
     * Gets whether this component is in obfuscation.
     *
     * @return True iff it is.
     */
    public boolean isObfuscated() {
        Boolean flag = this.obfuscated;
        return flag != null && flag;
    }

    /**
     * Sets this component's obfuscation.
     *
     * @param obfuscated The new obfuscated state.
     * @return This component.
     */
    public ChatComponent setObfuscated(boolean obfuscated) {
        this.obfuscated = obfuscated;
        return this;
    }

    /**
     * Tries to filter out every single use of color-codes and replaces them with the {@link ChatColor}s
     * It filters through every text-sequence of this component and if it finds a color-code-usage
     * it will append a new {@link ChatComponent} with the found text and the found chat color
     *
     * @param colorChars the chars to filter for
     * @return newly created component
     */
    public ChatComponent transformColors(char... colorChars) {

        if (text == null || Stream.of(colorChars).noneMatch(c -> text.contains(String.valueOf(c)))) {
            return this;
        }

        ChatComponent component = new ChatComponent();
        component.setText("");
        for (char colorChar : colorChars) {
            for (String s : this.text.split(String.valueOf(colorChar))) {

                if (s.trim().isEmpty()) {
                    continue;
                }

                String text = s.substring(1);
                String c = s.split(text)[0];
                ChatColor chatColor = ChatColor.of(c);
                component.append(new ChatComponent().setText(text).setColor(chatColor));
            }

            for (ChatComponent chatComponent : this.extra) {
                component.append(chatComponent.transformColors(colorChar));
            }

        }
        return component;
    }

    /**
     * Replaces a given value with a given value in this {@link ChatComponent}
     * and does the same recursive in all the sub-components in the extra array
     *
     * @param search the value to replace
     * @param replaceWith the value that should be replaced with
     * @return current component
     */
    public ChatComponent replace(String search, String replaceWith) {

        if (text == null || !text.contains(search)) {
            return this;
        }

        this.text = this.text.replace(search, replaceWith);

        for (ChatComponent component : this.extra) {
            int index = this.extra.indexOf(component);
            this.extra.set(index, component.replace(search, replaceWith));
        }

        return this;
    }

    /**
     * Gets this component as a JSON element, ready to be sent to a client.
     *
     * @return The JSON element.
     */
    public Document toDocument() {
        Document json = DocumentFactory.newJsonDocument();

        String text = this.text;
        if (text != null) {
            json.set("text", text);
        }

        String translate = this.translate;
        if (translate != null) {
            json.set("translate", translate);
            Bundle array = DocumentFactory.newJsonBundle();
            this.with.forEach(e -> array.add(e.toDocument()));
            json.set("with", array);
        }

        String scoreUsername = this.scoreUsername;
        String scoreObjective = this.scoreObjective;
        if (scoreUsername != null && scoreObjective != null) {
            Document score = DocumentFactory.newJsonDocument();
            score.set("name", scoreUsername);
            score.set("objective", scoreObjective);
            json.set("score", score);
        }

        String selector = this.selector;
        if (selector != null) {
            json.set("selector", selector);
        }

        List<ChatComponent> extra = this.extra;
        if (!extra.isEmpty()) {
            Bundle extraArray = DocumentFactory.newJsonBundle();
            extra.forEach(e -> extraArray.add(e.toDocument()));
            json.set("extra", extraArray);
        }

        Boolean isBold = this.bold;
        if (isBold != null) {
            json.set("bold", isBold);
        }

        Boolean isItalic = this.italic;
        if (isItalic != null) {
            json.set("italic", isItalic);
        }

        Boolean isUnderlined = this.underlined;
        if (isUnderlined != null) {
            json.set("underlined", isUnderlined);
        }

        Boolean isStrikethrough = this.strikethrough;
        if (isStrikethrough != null) {
            json.set("strikethrough", isStrikethrough);
        }

        Boolean isObfuscated = this.obfuscated;
        if (isObfuscated != null) {
            json.set("obfuscated", isObfuscated);
        }

        ChatColor color = this.color;
        if (color != null && !color.isFormat()) {
            json.set("color", color.name().toLowerCase());
        }

        ClickEvent clickEvent = this.clickEvent;
        if (clickEvent != null) {
            json.set("clickEvent", clickEvent.asJson());
        }

        HoverEvent hoverEvent = this.hoverEvent;
        if (hoverEvent != null) {
            json.set("hoverEvent", hoverEvent.asJson());
        }

        String insertion = this.insertion;
        if (insertion != null) {
            json.set("insertion", insertion);
        }
        return json;
    }

    /**
     * Gets this component as a JSON string.
     *
     * @return The JSON string.
     */
    public String toString() {
        return this.toDocument().asRawJsonString();
    }

    public String toLegacyText() {
        StringBuilder builder = new StringBuilder();
        if (bold != null && bold) {
            builder.append("§l");
        }
        if (italic != null && italic) {
            builder.append("§n");
        }
        if (strikethrough != null && strikethrough) {
            builder.append("§m");
        }
        if (obfuscated != null && obfuscated) {
            builder.append("§k");
        }
        if (underlined != null && underlined) {
            builder.append("§n");
        }
        if (this.color != null) {
            builder.append(this.color.format());
        }
        if (this.text != null) {
            builder.append(this.text);
        }
        for (ChatComponent component : extra) {
            builder.append(" ").append(component.toLegacyText());
        }

        return ChatColor.translateAlternateColorCodes('&', builder.toString());
    }

    /**
     * Creates a new empty component.
     *
     * @return The new component.
     */
    public static ChatComponent create() {
        return new ChatComponent();
    }

    /**
     * Creates a component with an empty string for text.
     *
     * @return The component.
     */
    public static ChatComponent empty() {
        return create().setText("");
    }

    /**
     * Creates a component with a given string for text.
     *
     * @param text The text.
     * @return The component.
     */
    public static ChatComponent text(String text) {
        return create().setText(text);
    }

    /**
     * Creates a component based on a given JSON object.
     *
     * @param json The JSON.
     * @return The component.
     */
    public static ChatComponent fromJson(Document json) {
        ChatComponent cc = create();
        IEntry text = json.getEntry("text");
        if (text != null) {
            cc.setText(text.toString());
        }
        IEntry translate = json.getEntry("translate");
        if (translate != null) {
            cc.setTranslate(translate.toString());
        }
        IEntry with = json.getEntry("with");
        if (with != null) {
            Bundle array = with.toBundle();
            for (int i = 0, j = array.size(); i < j; i++) {
                IEntry el = array.getEntry(i);
                if (el.isPrimitive())
                    cc.addWith(el.toString());
                else if (el.isDocument())
                    cc.addWith(fromJson(el.toDocument()));
            }
        }
        IEntry score = json.getEntry("score");
        if (score != null) {
            Document scoreArray = score.toDocument();
            cc.setScoreUsername(scoreArray.getString("name"));
            cc.setScoreObjective(scoreArray.getString("objective"));
        }
        IEntry selector = json.getEntry("selector");
        if (selector != null) {
            cc.setSelector(selector.toString());
        }
        IEntry extra = json.getEntry("extra");
        if (extra != null) {
            Bundle array = extra.toBundle();
            for (int i = 0, j = array.size(); i < j; i++) {
                IEntry el = array.getEntry(i);
                if (el.isPrimitive())
                    cc.append(new StringChatComponent(el.toString()));
                else if (el.isDocument())
                    cc.append(fromJson(el.toDocument()));
            }
        }
        IEntry bold = json.getEntry("bold");
        if (bold != null) {
            cc.setBold(bold.toBoolean());
        }
        IEntry italic = json.getEntry("italic");
        if (italic != null) {
            cc.setItalic(italic.toBoolean());
        }
        IEntry underlined = json.getEntry("underlined");
        if (underlined != null) {
            cc.setUnderlined(underlined.toBoolean());
        }
        IEntry strikethrough = json.getEntry("strikethrough");
        if (strikethrough != null) {
            cc.setStrikethrough(strikethrough.toBoolean());
        }
        IEntry obfuscated = json.getEntry("obfuscated");
        if (obfuscated != null) {
            cc.setObfuscated(obfuscated.toBoolean());
        }
        IEntry color = json.getEntry("color");
        if (color != null) {
            cc.setColor(ChatColor.valueOf(color.toString().toUpperCase()));
        }
        IEntry clickEvent = json.getEntry("clickEvent");
        if (clickEvent != null) {
            cc.setClickEvent(ClickEvent.fromJson(clickEvent.toDocument()));
        }
        IEntry hoverEvent = json.getEntry("hoverEvent");
        if (hoverEvent != null) {
            cc.setHoverEvent(HoverEvent.fromJson(hoverEvent.toDocument()));
        }
        IEntry insertion = json.getEntry("insertion");
        if (insertion != null) {
            cc.setInsertion(insertion.toString());
        }
        return cc;
    }

    /**
     * Builds a component from a format string containing color codes.
     *
     * @param format The format string.
     * @return The component.
     */
    public static ChatComponent fromFormat(String format) {
        char[] chars = format.toCharArray();
        String currentText = "";
        ChatColor currentColor = null;
        ChatComponent component = ChatComponent.create(), currentComponent = null;
        boolean bold, italic, underline, strikethrough, obfuscate;
        bold = italic = underline = strikethrough = obfuscate = false;
        for (int i = 0, j = chars.length; i < j; i++) {
            boolean prevSection = i != 0 && chars[i - 1] == '\u00A7';
            char c = chars[i];
            if (prevSection) {
                ChatColor color = ChatColor.of(c);
                if (color != null) {
                    ChatComponent curr = currentComponent == null ? component : currentComponent;
                    // splice off current component
                    if (!currentText.isEmpty()) {
                        curr.setText(currentText).setColor(currentColor);
                        if (currentComponent != null)
                            component.append(currentComponent);
                        curr = currentComponent = create();
                        currentText = "";
                    }
                    if (color.isColor()) {
                        currentColor = color;
                        // disable all formatting
                        if (bold)
                            curr.setBold(bold = false);
                        if (italic)
                            curr.setItalic(italic = false);
                        if (underline)
                            curr.setUnderlined(underline = false);
                        if (strikethrough)
                            curr.setStrikethrough(strikethrough = false);
                        if (obfuscate)
                            curr.setObfuscated(obfuscate = false);
                    } else {
                        // formatting code
                        switch (color) {
                            case BOLD:
                                if (!bold) {
                                    curr.setBold(bold = true);
                                }
                                break;
                            case ITALIC:
                                if (!italic) {
                                    curr.setItalic(italic = true);
                                }
                                break;
                            case UNDERLINE:
                                if (!underline) {
                                    curr.setUnderlined(underline = true);
                                }
                                break;
                            case STRIKETHROUGH:
                                if (!strikethrough) {
                                    curr.setStrikethrough(strikethrough = true);
                                }
                                break;
                            case OBFUSCATED:
                                if (!obfuscate) {
                                    curr.setObfuscated(obfuscate = true);
                                }
                                break;
                            case RESET:
                                // remove current color
                                currentColor = null;
                                // disable all formatting
                                if (bold)
                                    curr.setBold(bold = false);
                                if (italic)
                                    curr.setItalic(italic = false);
                                if (underline)
                                    curr.setUnderlined(underline = false);
                                if (strikethrough)
                                    curr.setStrikethrough(strikethrough = false);
                                if (obfuscate)
                                    curr.setObfuscated(obfuscate = false);
                                break;
                        }
                    }
                }
            } else if (c != '\u00A7') {
                currentText += c;
            }
        }
        if (!currentText.isEmpty()) {
            component.append(currentComponent.setText(currentText).setColor(currentColor));
        }
        return component;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || o == this || o.getClass() != this.getClass()) {
            return o == this;
        }
        ChatComponent cc = (ChatComponent) o;
        return this.toDocument().equals(cc.toDocument());
    }

    @Override
    public int hashCode() {
        return this.toDocument().hashCode();
    }

    @Override
    public void applyBuffer(BufferState state, @NotNull PacketBuffer buf) throws IOException {
        switch (state) {
            case WRITE:
                buf.writeDocument(this.toDocument());
                break;
            case READ:
                ChatComponent component = ChatComponent.fromJson(buf.readDocument());

                //general settings
                this.text = component.text;
                this.color = component.color;
                this.insertion = component.insertion;
                this.translate = component.translate;
                this.selector = component.selector;

                //scoreboard management
                this.scoreObjective = component.scoreObjective;
                this.scoreUsername = component.scoreUsername;

                //lists
                this.extra.addAll(component.extra);
                this.with.addAll(component.with);

                //style of text
                this.bold = component.bold;
                this.underlined = component.underlined;
                this.strikethrough = component.strikethrough;
                this.italic = component.italic;
                this.obfuscated = component.obfuscated;

                //events
                this.clickEvent = component.clickEvent;
                this.hoverEvent = component.hoverEvent;
                break;
        }
    }

    @Getter
    @AllArgsConstructor
    private static class StringChatComponent extends ChatComponent {

        private String string;

        @Override
        public Document toDocument() {
            return DocumentFactory.newJsonDocument(string);
        }
    }
}
