package cloud.hytora.driver.component;

import cloud.hytora.driver.common.UniqueReturnValue;
import cloud.hytora.driver.component.base.TextBased;
import cloud.hytora.driver.component.event.ComponentEvent;
import cloud.hytora.driver.component.event.click.ClickEvent;
import cloud.hytora.driver.component.event.hover.HoverAction;
import cloud.hytora.driver.component.event.hover.HoverEvent;
import cloud.hytora.driver.component.feature.ComponentOption;
import cloud.hytora.driver.component.feature.tablist.TabList;
import cloud.hytora.driver.component.feature.title.Title;
import cloud.hytora.driver.component.style.ComponentColor;
import cloud.hytora.driver.component.style.ComponentStyle;
import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public interface Component extends TextBased<Component>, IBufferObject {

    /**
     * Creates a new {@link Component} with a pre-given text
     * and a already selected {@link ComponentColor} for the text
     *
     * @param text the text to display
     * @param color the color for the text
     * @return built component
     */
    static Component text(String text, ComponentColor color) {
        return text(text).color(color);
    }

    /**
     * Creates a {@link Component} with already pre-given text
     *
     * @param text the content to put in
     */
    static Component text(String text) {
        return empty().setContent(text);
    }

    /**
     * Creates a new {@link Component} with pre given texts
     * Might be useful for other {@link TextBased} values
     * If you want to transform this {@link Component} to them
     *
     * @param texts the text
     * @return created component
     */
    static Component text(String... texts) {
        StringBuilder componentText = new StringBuilder();
        for (int i = 0; i < texts.length; i++) {
            componentText.append(texts[i]);
            if (i != (texts.length - 1)) {
                componentText.append(" ");
            } else {
                //Last arg reached no space needed
            }
        }

        return text(componentText.toString()).put(ComponentOption.COMPONENT_TEXTS, texts);
    }

    /**
     * Creates a new empty {@link Component}
     */
    static Component empty() {
        return new SimpleComponent();
    }

    /**
     * Sets the content of this component
     *
     * @param content the content to set
     * @return current component
     */
    Component setContent(String content);

    /**
     * Sets the color of this {@link Component}
     *
     * @param color the color to set
     * @return current component
     */
    Component color(ComponentColor color);

    /**
     * Sets extra styles of this {@link Component}
     *
     * @param style the style to set
     * @return current component
     */
    Component style(ComponentStyle style);

    /**
     * Adds a {@link ComponentEvent} to this component
     *
     * @param event the event to add
     * @return current component
     */
    Component event(ComponentEvent<?> event);

    /**
     * Adds another component to chain
     *
     * @param component the component to add
     * @return this component
     */
    Component append(Component component);

    /**
     * Appends a spacer to this component
     *
     * @return current component
     */
    Component spacer();

    @Nullable
    ComponentEvent<HoverEvent> getHoverEvent();

    @Nullable
    ComponentEvent<ClickEvent> getClickEvent();

    @UniqueReturnValue
    @Nonnull
    Collection<Component> getSubComponents();

    /**
     * Gets a sub {@link Component}
     *
     * @param index the index of the comp
     * @return component or null if not found or index out of bounds
     */
    Component get(int index);

    /**
     * The content of this component
     */
    String getContent();

    /**
     * Transforms this {@link Component} into a {@link Title}
     */
    Title toTitle();

    /**
     * Transforms this {@link Component} into a {@link TabList}
     */
    TabList toTabList();
}
