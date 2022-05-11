package cloud.hytora.driver.setup;

public interface SetupInputParser<T> {

    T parse(SetupEntry entry, String input);
}
