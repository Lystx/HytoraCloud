package cloud.hytora.driver.reaction;

import lombok.Getter;

import java.util.Arrays;
import java.util.function.Consumer;

@Getter
public abstract class GenericResult<E, V extends GenericResult<E, V>> {

    /**
     * The key of this result
     */
    protected String key;

    /**
     * The id to identify
     */
    protected int id;

    /**
     * The result
     */
    protected E result;

    /**
     * The self generic instance that might extend this class
     */
    public abstract V getSelf();

    /**
     * Checks if any of the provided keys matches the current key
     *
     * @param keys the keys-array to check
     * @return boolean
     */
    public boolean containsAnyKey(String... keys) {
        return keys.length != 0 && Arrays.stream(keys).anyMatch(s -> key.equalsIgnoreCase(s) || key.endsWith(s));
    }

    /**
     * Executes given procedure if the id=getId and if the key=getKey
     *
     * @param id        The id of the procedure
     * @param procedure The procedure itself
     * @param keys      The keys (only one of them must be correct)
     */
    public void execute(int id, Consumer<V> procedure, String... keys) {
        if ((keys.length == 0 || containsAnyKey(keys)) && id == getId()) {
            procedure.accept(this.getSelf());
        }
    }

    /**
     * Sets the result ignoring the current id of this instance
     *
     * @param element the result to set
     * @param keys the keys to check
     */
    @Deprecated
    public void setResult(E element, String... keys) {
        if (keys.length == 0 || containsAnyKey(keys)) {
            this.result = element;
        }
    }

    /**
     * Sets the result of this instance if the provided id matches with the own one
     *
     * @param id the id to check
     * @param element the result to set
     * @param keys the keys to check
     */
    public void setResult(int id, E element, String... keys) {
        this.execute(id, self -> this.result = element, keys);
    }

    /**
     * Sets the result of this instance if the provided id matches with the own one
     * ignoring any keys that should be checked
     *
     * @param id the id to check
     * @param element the result to set
     */
    public void setResult(int id, E element) {
        this.setResult(id, element, new String[]{});
    }

}
