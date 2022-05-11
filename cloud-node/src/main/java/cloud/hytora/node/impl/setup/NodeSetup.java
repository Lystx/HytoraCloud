package cloud.hytora.node.impl.setup;

import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.annotations.*;
import cloud.hytora.node.impl.database.DatabaseType;
import lombok.Getter;

@Getter
public class NodeSetup extends Setup<NodeSetup> {

    @Question(id = 1, question = "What should this Node be called?")
    @QuestionTip("Use something like 'Node-' and a number behind it")
    private String name;

    @Question(id = 2, question = "Which host should this Node bind to?")
    @QuestionTip("Default is 127.0.0.1")
    @SuggestedAnswer("127.0.0.1")
    private String host;

    @Question(id = 3, question = "Which port should this Node bind to?")
    @QuestionTip("Consider your database ports to be free")
    @SuggestedAnswer("2704")
    private int port;

    @Question(id = 4, question = "On which port should Services start?")
    @SuggestedAnswer("30000")
    private int serviceStartPort;

    @Question(id = 5, question = "Which database do you want to use?")
    @QuestionTip("When using multiple Nodes, you should use an online database!")
    @RequiresEnum(DatabaseType.class)
    @ExitAfterInput("FILE")
    private DatabaseType databaseType;

    @Question(id = 6, question = "What's the host of your database?")
    private String databaseHost;

    @Question(id = 7, question = "What's the port of your database?")
    private int databasePort;

    @Question(id = 7, question = "What's the username of your database?")
    private String databaseUser;

    @Question(id = 8, question = "What's the password of your database?")
    private String databasePassword;

    @Question(id = 9, question = "What's the name of your database?")
    private String databaseName;

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
