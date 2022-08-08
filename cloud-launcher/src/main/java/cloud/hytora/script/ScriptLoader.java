package cloud.hytora.script;

import cloud.hytora.common.misc.FileUtils;
import cloud.hytora.common.misc.StringUtils;
import cloud.hytora.common.task.Task;

import java.io.BufferedReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

// TODO: 21.02.2022 documentation
public class ScriptLoader {


    public static ScriptLoader getInstance() {
        return instance == null ? new ScriptLoader() : instance;
    }

    private static ScriptLoader instance;

    private final Map<String, ScriptCommand> commands;

    private ScriptLoader() {
        instance = this;
        this.commands = new LinkedHashMap<>();
    }


    public void registerCommand(String name, ScriptCommand command) {
        commands.put(name, command);
    }

    public void registerCommand(ScriptCommand command, String... names) {
        for (String name : names) {
            registerCommand(name, command);
        }
    }

    public Task<Void> executeScript(Path path) throws IOException {
        return executeScriptFromReader(FileUtils.newBufferedReader(path));
    }

    public Task<Void> executeScriptFromInputStream(InputStream input) throws IOException {
        return executeScriptFromReader(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
    }

    public Task<Void> executeScriptFromResource(String resource) throws IOException {
        InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(resource);
        assert systemResourceAsStream != null;
        return this.executeScriptFromReader(new BufferedReader(new InputStreamReader(systemResourceAsStream, StandardCharsets.UTF_8)));
    }

    public Task<Void> executeScriptFromReader(BufferedReader reader) throws IOException {
        return Task.callAsync(() -> {

            String line;
            while ((line = reader.readLine()) != null) {
                executeLine(line);
            }
            return null;
        });
    }

    public String replaceScriptVariables(String line) {

        if (line.contains("{@")) {
            String placeHolder = line.split("\\{@")[1];
            if (!placeHolder.contains("}")) {
                throw new IllegalStateException("Can't declare variable with no closing Bracket '}'");
            }
            String finalPlaceHolderName = placeHolder.split("}")[0];
            String reversedPlaceHolder = "{@" + finalPlaceHolderName + "}";

            String var = System.getProperty(finalPlaceHolderName, "<not_set>");

            line = line.replace(reversedPlaceHolder, var);
        }
        return line;
    }

    private void executeLine(String line) {
        if (line.startsWith("#") || line.trim().isEmpty()) {
            //comments or empty line -> ignoring
            return;
        }
        if (line.startsWith("var")) {

            //replacing line variables
            line = replaceScriptVariables(line);

            String[] args = line.split(" ");

            if (args.length == 1) {
                args = new String[0];
            } else {
                args = Arrays.copyOfRange(args, 1, args.length);
            }

            System.setProperty(args[0], String.join(" ", Arrays.copyOfRange(args, 1, args.length)));
        } else {

            //replacing line variables
            line = replaceScriptVariables(line);
            if (!line.contains("(") && !line.contains(")")) {
                throw new IllegalStateException("CommandLines have to start with '(' and have to end with ');'!");
            }
            if (!line.endsWith(";")) {
                throw new IllegalStateException("CommandLines have to end with ';'!");
            }
            String[] data = line.split("\\(");
            String commandName = data[0];
            String commandLine = data[1];

            String arguments = StringUtils.getBetween(line, "(", ");");

            if (!arguments.startsWith("'") && !arguments.endsWith("'")) {
                throw new IllegalStateException("Arguments have to be between \"'\"!");
            }
            arguments = StringUtils.getBetween(arguments, "'", "'");

            String[] args = arguments.split(" ");

            if (args.length != 1) {
                args = Arrays.copyOfRange(args, 0, args.length);
            }

            ScriptCommand command = commands.get(commandName);
            if (command == null) {
                throw new IllegalStateException("Unsupported syntax '" + line + "'");
            }
            System.out.println(commandName + ": " + Arrays.toString(args));
            command.execute(args, String.join(" ", args), arguments);
        }

    }
}
