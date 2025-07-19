package cloud.hytora.node.impl.setup;

import cloud.hytora.driver.command.console.Console;
import cloud.hytora.driver.common.setup.Setup;
import cloud.hytora.driver.common.setup.annotations.Question;
import cloud.hytora.driver.common.setup.annotations.QuestionTip;
import cloud.hytora.driver.database.api.DatabaseType;
import lombok.Getter;

@Getter
public class NodeRemoteSetup extends Setup<NodeRemoteSetup> {

    private final String name;
    private final boolean remote;
    private final DatabaseType databaseType;
    private final long memory;

    public NodeRemoteSetup(String name, boolean remote, DatabaseType databaseType, long memory) {
        this.name = name;
        this.remote = remote;
        this.databaseType = databaseType;
        this.memory = memory;
    }

    @Question(id = 1, question = "What is the host of the Node you want this Node to connect to?")
    private String host;

    @Question(id = 2, question = "What is the port of the Node you want this Node to connect to?")
    private int port;

    @Question(id = 3, question = "What is the auth key of the Node you want this Node to connect to?")
    @QuestionTip("Look in the config.json!")
    private String authKey;


    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
