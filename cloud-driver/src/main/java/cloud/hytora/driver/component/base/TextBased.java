package cloud.hytora.driver.component.base;

import cloud.hytora.driver.component.feature.ComponentOption;

/**
 * Just to declare all component
 * values that they are text based
 * to allow multiple implementations
 */
public interface TextBased<B extends TextBased<B>> {

    /**
     * Puts a {@link ComponentOption} to this base
     *
     * @param option the option
     * @param t the value for the option
     * @param <T> the generic
     * @return current component
     */
    <T> B put(ComponentOption<B, T> option, T t);

}
