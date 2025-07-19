package cloud.hytora.node.impl.setup;

import cloud.hytora.driver.common.setup.Setup;
import cloud.hytora.driver.common.setup.annotations.*;
import cloud.hytora.driver.common.setup.suggesters.EnumSuggester;
import cloud.hytora.driver.database.api.DatabaseType;
import lombok.Getter;

@Getter
public class NetworkSetup extends Setup<NetworkSetup> {

    @Question(id = 1, question = "What type of node do you want to set up?")
    @RequiresEnum(NodeType.class)
    @AnswerCompleter(EnumSuggester.class)
    @QuestionTip("'STANDALONE' = Main-Node that can operate by itsself | 'SLAVE' = Sub-Node that receives commands from Head-Node")
    private NodeType nodeType;

    @Question(id = 2, question = "What should this Node be called?")
    @QuestionTip("Use something like 'Node-' and a number behind it")
    private String name;

    @Question(id = 3, question = "How much memory is this Node allowed to use at maximum?")
    @QuestionTip("Memory is always in MegaBytes (MB)")
    private long memory;

    @Question(id = 4, question = "Which database do you want to use?")
    @QuestionTip("When using multiple Nodes, you have to use an online database!")
    @RequiresEnum(DatabaseType.class)
    @AnswerCompleter(EnumSuggester.class)
    private DatabaseType databaseType;


    @Override
    public boolean isCancellable() {
        return false;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }


    public enum NodeType {

        STANDALONE,

        SLAVE,

    }
}
