package cloud.hytora.node.console.jline2.completer;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.driver.console.TabCompleter;
import jline.console.completer.Completer;

import java.util.List;

public class CandidateCompleter implements Completer {

    @Override
    public int complete(String buffer, int cursor, List<CharSequence> candidates) {

        ScreenManager screenManager = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        Screen screen = screenManager.getCurrentScreen();
        if (screen != null) {
            TabCompleter currentTabCompleter = screen.getCurrentTabCompleter();
            if (currentTabCompleter != null) {
                return currentTabCompleter.onTabComplete(buffer, cursor, candidates);
            }
        }
        return -1;
    }

}
