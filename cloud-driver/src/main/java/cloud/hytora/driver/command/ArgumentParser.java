package cloud.hytora.driver.command;

public interface ArgumentParser<T> {

    T parse(String input) throws Exception;
}
