package cloud.hytora.node.impl.setup;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.console.Console;
import cloud.hytora.driver.services.task.ICloudServiceTaskManager;
import cloud.hytora.driver.services.utils.version.ServiceVersion;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.annotations.*;
import cloud.hytora.driver.setup.suggesters.BooleanSuggester;
import cloud.hytora.driver.setup.suggesters.EnumSuggester;
import lombok.Getter;

@Getter
public class TaskSetup extends Setup<TaskSetup> {

    @Question(id = 1, question = "What should this task be named?")
    @ConditionChecker(value = Checker.class, message = "There is already a Task with the name '%input%'")
    private String name;

    @Question(id = 2, question = "What TaskGroup does this task belong to?")
    @QuestionTip("If you don't know what this means, just re-enter the name of the task!")
    private String parentName;

    @Question(id = 3, question = "What serversoftware should this task use?")
    @RequiresEnum(ServiceVersion.class)
    @AnswerCompleter(EnumSuggester.class)
    private ServiceVersion version;

    @Question(id = 4, question = "Should services of this task be dynamic?")
    @QuestionTip("Dynamic means that the service and all its data will be deleted on shutdown")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean dynamic;

    @Question(id = 5, question = "How much memory are services of this task allowed to use?")
    private int memory;

    @Question(id = 6, question = "How many players are on each service of this task allowed to be?")
    private int maxPlayers;

    @Question(id = 7, question = "How many services of this task may maximum be online?")
    @QuestionTip("Use -1 for unlimited")
    private int maxServers;

    @Question(id = 8, question = "How many services of this task have to be always online?")
    @QuestionTip("Minimum = 1")
    private int minServers;

    @Question(id = 9, question = "On which Node(s) should this task be able to run?")
    @QuestionTip("Separate multiple Nodes with a \",\"")
    private String node;

    @Question(id = 10, question = "Which TemplateStorage should Services of this task use?")
    @QuestionTip("Default is 'local'")
    private String templateStorage;

    @Question(id = 11, question = "Should this group be in maintenance?")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean maintenance;

    @Question(id = 12, question = "What JavaVersion should this task use?")
    @QuestionTip("Use '-1' for default java on virtual machine")
    @SuggestedAnswer("-1")
    private int javaVersion;

    @Question(id = 13, question = "What is the startOrder of this task?")
    @QuestionTip("The lower the startOrder the higher it will be ranked in selection of service starting")
    private int startOrder;

    @Question(id = 14, question = "Is this group a Fallback?")
    @ExitAfterInput("false")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean fallback;

    @Question(id = 15, question = "Whats the priority of this fallback?")
    private int fallbackPriority;

    @Question(id = 16, question = "Whats the permission players need to access this fallback?")
    @QuestionTip("Use 'none' for no permission")
    @SuggestedAnswer("none")
    private String fallbackPermission;

    public TaskSetup(Console console) {
        super();
    }

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }


    public static class Checker implements BiSupplier<String, Boolean> {

        @Override
        public Boolean supply(String name) {
            return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ICloudServiceTaskManager.class).getTaskOrNull(name) != null;
        }
    }

}
