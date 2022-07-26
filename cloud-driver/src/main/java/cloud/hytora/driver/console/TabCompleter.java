package cloud.hytora.driver.console;

import java.util.Collection;

public interface TabCompleter {


    Collection<String> onTabComplete(String buffer);
}
