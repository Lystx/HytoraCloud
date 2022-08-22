package cloud.hytora.modules.perms.cloud.setup;

import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.annotations.Question;
import cloud.hytora.driver.setup.annotations.QuestionTip;
import lombok.Getter;

@Getter
public class GroupSetup extends Setup<GroupSetup> {

    @Question(id = 1, question = "What should this group be named?")
    private String name;

    @Question(id = 2, question = "What's the priority of this group?")
    @QuestionTip("The higher the priority = lower value (e.g. 9999 = bad | 0 = good)")
    private int sortId;

    @Question(id = 3, question = "Should this group be a default group that every new player receives?")
    private boolean defaultGroup;

    @Question(id = 4, question = "What should the prefix of this group be?")
    private String prefix;

    @Question(id = 5, question = "What should the suffix of this group be?")
    private String suffix;

    @Question(id = 6, question = "What should the chatColor of this group be?")
    private String chatColor;

    @Question(id = 7, question = "What should the general color of this group be?")
    private String color;

    @Question(id = 8, question = "What groups should this group extend from?")
    @QuestionTip("Separate multiple groups with a \",\"")
    private String inheritedGroups;

    @Override
    public boolean isCancellable() {
        return true;
    }

    @Override
    public boolean shouldPrintHeader() {
        return true;
    }
}
