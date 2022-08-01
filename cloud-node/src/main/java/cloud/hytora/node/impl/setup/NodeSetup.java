package cloud.hytora.node.impl.setup;

import cloud.hytora.driver.command.Console;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.annotations.*;
import cloud.hytora.node.impl.database.config.DatabaseType;
import lombok.Getter;

@Getter
public class NodeSetup extends Setup<NodeSetup> {

    @Question(id = 1, question = "What should this Node be called?")
    @QuestionTip("Use something like 'Node-' and a number behind it")
    @SuggestedAnswer("Node-")
    private String name;

    @Question(id = 2, question = "Which host should this Node bind to?")
    @QuestionTip("Default is 127.0.0.1")
    @SuggestedAnswer("127.0.0.1")
    private String host;

    @Question(id = 3, question = "Which port should this Node bind to?")
    @QuestionTip("Consider your database ports to be free")
    @SuggestedAnswer("2704")
    private int port;

    @Question(id = 4, question = "Is this Node a Remote (Slave) in the Cluster?")
    @QuestionTip("A Remote receives commands and connects to the HeadNode")
    private boolean remote;

    @Question(id = 5, question = "How much memory is this Node allowed to use at maximum?")
    @QuestionTip("Memory is always in MegaBytes (MB)")
    private long memory;

    @Question(id = 6, question = "Do you want to create a default Lobby & Proxy Task?")
    private boolean defaultTasks;

    @Question(id = 7, question = "Which database do you want to use?")
    @QuestionTip("When using multiple Nodes, you should use an online database!")
    @RequiresEnum(DatabaseType.class)
    private DatabaseType databaseType;


    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
