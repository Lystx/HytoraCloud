package cloud.hytora.node.impl.setup;

import cloud.hytora.driver.common.setup.Setup;
import cloud.hytora.driver.common.setup.annotations.*;
import cloud.hytora.driver.database.api.DatabaseType;
import cloud.hytora.driver.common.setup.suggesters.BooleanSuggester;
import lombok.Getter;

@Getter
public class NodeSetup extends Setup<NodeSetup> {

    private final String name;
    private final boolean remote;
    private final DatabaseType databaseType;
    private final long memory;

    public NodeSetup(String name, boolean remote, DatabaseType databaseType, long memory) {
        this.name = name;
        this.remote = remote;
        this.databaseType = databaseType;
        this.memory = memory;
    }

    @Question(id = 1, question = "Which host should this Node bind to?")
    @SuggestedAnswer("127.0.0.1")
    @QuestionTip("Default is 127.0.0.1")
    private String host;

    @Question(id = 2, question = "Which port should this Node bind to?")
    @QuestionTip("Consider your database ports to be free")
    @SuggestedAnswer("2704")
    private int port;

    @Question(id = 3, question = "Do you want to create a default Lobby & Proxy Task?")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean defaultTasks;




    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
