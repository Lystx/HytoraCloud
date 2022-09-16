package cloud.hytora.node.console.jline3;


import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.ICommandManager;
import cloud.hytora.driver.console.TabCompleter;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.node.NodeDriver;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class JLine3Completer implements Completer {

    @Override
    public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {

        String buffer = line.line();
        ScreenManager sm = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);
        if (sm.isCurrentlyInScreen()) {
            TabCompleter tc = sm.getCurrentScreen().getCurrentTabCompleter();
            if (tc != null) {
                candidates.addAll(tc.onTabComplete(buffer).stream().map(Candidate::new).collect(Collectors.toList()));
            }
        }
    }
}
