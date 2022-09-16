package cloud.hytora.node.console;

import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.commands.events.TabCompleteEvent;
import cloud.hytora.driver.console.TabCompleter;
import cloud.hytora.driver.event.IEventManager;
import com.google.common.base.Preconditions;

import java.util.*;

public class NodeCommandCompleter implements TabCompleter {

    @Override
    public Collection<String> onTabComplete(String buffer) {

        Collection<String> result = new ArrayList<>();

        SortedSet<String> strings = new TreeSet<>();

        TabCompleteEvent event = new TabCompleteEvent(buffer);
        CloudDriver.getInstance().getProviderRegistry().getUnchecked(IEventManager.class).callEventGlobally(event);
        if (!event.isCancelled()) {
            Collection<String> l = event.getSuggestions();
            if (l != null) strings.addAll(l);
        }

        String currentBuffer = event.getCurrentBuffer();
        String add = event.getBeforeBuffer();

        if (currentBuffer == null) {
            for (String string : strings) {
                result.add(add + string);
            }
        } else {
            for (String match : strings.tailSet(currentBuffer)) {
                if (!match.startsWith(currentBuffer)) {
                    break;
                }
                result.add(add + match);
            }
        }

        return result;
    }
}
