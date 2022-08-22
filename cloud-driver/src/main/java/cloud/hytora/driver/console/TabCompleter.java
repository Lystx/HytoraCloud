package cloud.hytora.driver.console;

import java.util.List;

public interface TabCompleter {


    int onTabComplete(String buffer, int cursor, List<CharSequence> result);
}
