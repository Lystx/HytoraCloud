package cloud.hytora.node.impl.setup;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.services.utils.ServiceVersion;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.SetupHeaderBehaviour;
import cloud.hytora.driver.setup.annotations.*;
import cloud.hytora.driver.setup.suggesters.BooleanSuggester;
import cloud.hytora.driver.setup.suggesters.EnumSuggester;
import lombok.Getter;

@Getter
public class ConfigurationSetup extends Setup<ConfigurationSetup> {

    @Question(id = 1, question = "What should this configuration be named?")
    @ConditionChecker(value = Checker.class, message = "There is already a Configuration with the name '%input%'")
    private String name;

    @Question(id = 2, question = "What serversoftware should this configuration use?")
    @RequiresEnum(ServiceVersion.class)
    @AnswerCompleter(EnumSuggester.class)
    private ServiceVersion version;

    @Question(id = 3, question = "Should services of this configuration be dynamic?")
    @QuestionTip("Dynamic means that the service and all its data will be deleted on shutdown")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean dynamic;

    @Question(id = 4, question = "How much memory are services of this configuration allowed to use?")
    private int memory;

    @Question(id = 5, question = "How many players are on each service of this configuration allowed to be?")
    private int maxPlayers;

    @Question(id = 6, question = "How many services of this configuration may maximum be online?")
    @QuestionTip("Use -1 for unlimited")
    private int maxServers;

    @Question(id = 7, question = "How many services of this configuration have to be always online?")
    @QuestionTip("Minimum = 1")
    private int minServers;

    @Question(id = 8, question = "On which Node should this configuration run?")
    private String node;

    @Question(id = 9, question = "Should this group be in maintenance?")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean maintenance;

    @Question(id = 10, question = "What JavaVersion should this configuration use?")
    @QuestionTip("Use '-1' for default java on virtual machine")
    @SuggestedAnswer("-1")
    private int javaVersion;

    @Question(id = 11, question = "Is this group a Fallback?")
    @ExitAfterInput("false")
    @AnswerCompleter(BooleanSuggester.class)
    private boolean fallback;

    @Question(id = 12, question = "Whats the priority of this fallback?")
    private int fallbackPriority;

    @Question(id = 13, question = "Whats the permission players need to access this fallback?")
    @QuestionTip("Use 'none' for no permission")
    @SuggestedAnswer("none")
    private String fallbackPermission;

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }

    @Override
    public SetupHeaderBehaviour headerBehaviour() {
        return SetupHeaderBehaviour.RESTORE_PREVIOUS_LINES;
    }


    public static class Checker implements BiSupplier<String, Boolean> {

        @Override
        public Boolean supply(String name) {
            return CloudDriver.getInstance().getConfigurationManager().getConfigurationByName(name).isPresent();
        }
    }
}
