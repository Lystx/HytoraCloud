package cloud.hytora.driver.setup.suggesters;

import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.SetupEntry;
import cloud.hytora.driver.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;

public class BooleanSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.asList("true", "false");
    }
}
