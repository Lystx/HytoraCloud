package cloud.hytora.node;

import java.util.Collection;

public interface ServiceQueue {

    void dequeue();

    @Deprecated
    void queue();

    Collection<String> getPausedGroups();

    void addPausedGroup(String name);

    void removePausedGroup(String name);
}
