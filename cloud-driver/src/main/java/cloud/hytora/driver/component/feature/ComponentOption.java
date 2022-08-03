package cloud.hytora.driver.component.feature;

import cloud.hytora.driver.component.Component;
import cloud.hytora.driver.component.base.TextBased;
import cloud.hytora.driver.component.feature.tablist.TabList;
import cloud.hytora.driver.component.feature.title.Title;
import com.google.gson.internal.Primitives;
import lombok.*;

import java.util.Arrays;

@Getter @RequiredArgsConstructor
public class ComponentOption<B extends TextBased<B>, T> {

    //TITLE OPTIONS
    public static final ComponentOption<Title, Component> TITLE_SUBTITLE = new ComponentOption<>("subtitle", Component.class, Title.class);
    public static final ComponentOption<Title, Component> TITLE_MAIN_TITLE = new ComponentOption<>("title", Component.class, Title.class);
    public static final ComponentOption<Title, Integer> TITLE_FADE_IN = new ComponentOption<>("fadeIn", Integer.class, Title.class);
    public static final ComponentOption<Title, Integer> TITLE_STAY = new ComponentOption<>("stay", Integer.class, Title.class);
    public static final ComponentOption<Title, Integer> TITLE_FADE_OUT = new ComponentOption<>("fadeOut", Integer.class, Title.class);

    //TABLIST OPTIONS
    public static final ComponentOption<TabList, Component> TAB_HEADER = new ComponentOption<>("header", Component.class, TabList.class);
    public static final ComponentOption<TabList, Component> TAB_FOOTER = new ComponentOption<>("footer", Component.class, TabList.class);

    //GENERAL OPTION
    public static final ComponentOption<Component, String[]> COMPONENT_TEXTS = new ComponentOption<>("texts", String[].class, Component.class);

    /**
     * The name of the option
     */
    private final String name;

    /**
     * The type class of the option
     */
    private final Class<T> typeClass;

    /**
     * The class of the base that is needed for this option to use
     */
    private final Class<B> baseClass;

    /**
     * The value
     */
    @Setter
    private T value;

    public <E> E getValue(Class<E> eClass, E def) {
        if (value == null) {
            return def;
        } else {
            if (value.getClass().equals(eClass) || Arrays.asList(value.getClass().getInterfaces()).contains(eClass) || value.getClass().getSuperclass().equals(eClass) || value.getClass().equals(Primitives.unwrap(eClass)) || value.getClass().getSuperclass().equals(Primitives.unwrap(eClass))) {
                return (E) value;
            } else {
                return def;
            }
        }
    }

    public <E> E getValue(Class<E> eClass) {
        return getValue(eClass, null);
    }
}
