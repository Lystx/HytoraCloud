package cloud.hytora.driver.command.console;

import java.util.Collection;

public interface TabCompleter {


    Collection<String> onTabComplete(String buffer);
}
