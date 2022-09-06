package cloud.hytora.node.setup;

import cloud.hytora.driver.console.Console;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.annotations.Question;
import cloud.hytora.driver.setup.annotations.QuestionTip;
import lombok.Getter;

@Getter
public class NodeRemoteSetup extends Setup<NodeRemoteSetup> {

    @Question(id = 1, question = "What is the host of the Node you want this Node to connect to?")
    private String host;

    @Question(id = 2, question = "What is the port of the Node you want this Node to connect to?")
    private int port;

    @Question(id = 3, question = "What is the auth key of the Node you want this Node to connect to?")
    @QuestionTip("Look in the config.json!")
    private String authKey;

    public NodeRemoteSetup(Console console) {
        super();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
