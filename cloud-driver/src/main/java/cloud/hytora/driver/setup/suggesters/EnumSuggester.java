package cloud.hytora.driver.setup.suggesters;

import cloud.hytora.driver.setup.annotations.RequiresEnum;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.SetupEntry;
import cloud.hytora.driver.setup.SetupSuggester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class EnumSuggester implements SetupSuggester {
    
    @Override
    public List<String> suggest(Setup<?> setup, SetupEntry entry) {
        RequiresEnum requiresEnum = entry.getRequiresEnum();
        if (requiresEnum == null) {
            return new ArrayList<>();
        }
        Class<? extends Enum<?>> value = requiresEnum.value();
        return Arrays.stream(value.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
    }
}
