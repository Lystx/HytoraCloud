package cloud.hytora.driver.common.setup.suggesters;

import cloud.hytora.driver.common.setup.Setup;
import cloud.hytora.driver.common.setup.SetupEntry;
import cloud.hytora.driver.common.setup.SetupSuggester;
import cloud.hytora.driver.common.setup.annotations.RequiresEnum;

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
