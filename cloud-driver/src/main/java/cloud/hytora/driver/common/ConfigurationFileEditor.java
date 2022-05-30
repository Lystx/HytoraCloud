package cloud.hytora.driver.common;

import com.google.common.collect.Maps;
import com.google.common.io.Files;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigurationFileEditor {

    private final File file;

    private final List<String> listWithSpaces;

    private final List<String> lines;
    private final Map<String, String> keyToValues;

    private final ConfigSplitSpacer splitSpacer;

    @SneakyThrows
    public ConfigurationFileEditor(File file, final ConfigSplitSpacer splitSpacer) {
        this.listWithSpaces = Files.readLines(file, StandardCharsets.UTF_8);
        this.lines = this.listWithSpaces.stream().map(this::removeFirstSpaces).collect(Collectors.toList());

        this.file = file;

        this.keyToValues = Maps.newConcurrentMap();
        this.splitSpacer = splitSpacer;

        loadAllConfigOptions();
    }

    public void loadAllConfigOptions() {
        this.lines.stream()
                .filter(it -> it.contains(this.splitSpacer.getSplit()))
                .map(it -> it.split(this.splitSpacer.getSplit()))
                .forEach(it -> this.keyToValues.put(it[0], it.length == 1 ? "" : it[1]));
    }

    public String getValue(String key) {
        return this.keyToValues.get(key);
    }

    public void setValue(final String key, final String value) {
        keyToValues.put(key, value);
    }

    public void saveFile() {
        String[] property = this.lines.toArray(new String[]{});

        this.keyToValues.forEach((key, value) -> {
            String line = key + this.splitSpacer.getSplit() + value;
            int index = getIndexFromLine(line);
            property[index] = line;
        });

        this.file.delete();
        try {
            if (!this.file.exists()) file.createNewFile();
            FileWriter fileWriter = new FileWriter(this.file);
            for (final String line : property) {
                int index = this.getIndexFromLine(line);
                fileWriter.write(this.getStringWithSpaces(getAmountOfStartSpacesInLine(this.listWithSpaces.get(index))) + line + "\n");
            }
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getIndexFromLine(final String value) {
        int amountOfIndex = 0;
        for (final String line : this.lines) {
            if (value.contains(this.splitSpacer.getSplit())) {
                if (value.split(this.splitSpacer.getSplit())[0].equals(removeFirstSpaces(line).split(this.splitSpacer.getSplit())[0])) {
                    return amountOfIndex;
                }
            } else {
                if (value.equals(removeFirstSpaces(line))) {
                    return amountOfIndex;
                }
            }
            amountOfIndex++;
        }
        return -1;
    }

    public int getAmountOfStartSpacesInLine(final String line) {
        String lines = line;
        int amountOfSpaces = 0;
        while (lines.startsWith(" ")) {
            lines = lines.substring(1);
            amountOfSpaces++;
        }
        return amountOfSpaces;
    }

    public String removeFirstSpaces(final String string) {
        return string.substring(getAmountOfStartSpacesInLine(string));
    }

    private String getStringWithSpaces(int amount) {
        StringBuilder string = new StringBuilder();
        for (int i = 0; i < amount; i++) {
            string.append(" ");
        }
        return string.toString();
    }

}
