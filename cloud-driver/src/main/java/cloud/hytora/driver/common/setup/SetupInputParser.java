package cloud.hytora.driver.common.setup;

public interface SetupInputParser<T> {

    T parse(SetupEntry entry, String input);
}
