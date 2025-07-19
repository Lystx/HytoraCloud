package cloud.hytora.driver.common.setup.suggesters;

import cloud.hytora.driver.common.setup.Setup;
import cloud.hytora.driver.common.setup.SetupEntry;
import cloud.hytora.driver.common.setup.SetupSuggester;

import java.util.Arrays;
import java.util.List;

public class BooleanSuggester implements SetupSuggester {
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        return Arrays.asList("true", "false");
    }
}
