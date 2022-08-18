package cloud.hytora.script.api.impl;

import cloud.hytora.common.logging.Logger;
import cloud.hytora.script.api.IScript;
import cloud.hytora.script.api.IScriptCommand;
import cloud.hytora.script.api.IScriptLoader;

import cloud.hytora.script.api.IScriptTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class DefaultScriptLoader implements IScriptLoader {


    private static final String COMMENT_LINE_START = "#";
    private static final String TASK_LINE_START = "Task ";
    private static final String TASK_END = "} :";

    private final Collection<IScriptCommand> commands = new CopyOnWriteArrayList<>();

    @NotNull
    @Override
    public IScriptLoader registerCommand(@NotNull IScriptCommand command) {
        this.commands.add(command);
        return this;
    }

    @Nullable
    @Override
    public IScriptCommand getCommand(@NotNull String command) {
        return this.commands
                .stream()
                .filter(e -> e.getCommand().equalsIgnoreCase(command))
                .findFirst()
                .orElse(null);
    }


    @Nullable
    @Override
    public IScript loadScript(@NotNull Path script) {
        try {
            List<String> allLines = new CopyOnWriteArrayList<>(Files.readAllLines(script));
            Collection<String> comments = allLines
                    .stream()
                    .filter(e -> e.startsWith(COMMENT_LINE_START))
                    .collect(Collectors.toList());

            return this.loadScript(script, allLines, comments);
        } catch (final IOException ex) {
            Logger.constantInstance().error("Unable to read reform script located at {} : {}", script.toString(), ex);
        }

        return null;
    }

    boolean loadsTask = false;

    @Nullable
    private IScript loadScript(@NotNull Path path, @NotNull List<String> allLines, @NotNull Collection<String> comments) {
        Map<String, Map.Entry<Integer, IScriptCommand>> commandsPerLine = new LinkedHashMap<>();
        int cursorPosition = 0;
        List<String> taskLines = new ArrayList<>();

        for (String line : allLines) {
            if (comments.contains(line) || line.trim().isEmpty()) {
                cursorPosition++;
                continue;
            }

            if (line.startsWith(TASK_LINE_START)) {
                taskLines.add(line);
                loadsTask = true;
                cursorPosition++;
                continue;
            }
            if (loadsTask) {
                taskLines.add(line);
                cursorPosition++;
                continue;
            }

            try {
                IScriptCommand command = this.getCommandOfLine(line);
                if (command == null) {
                    cursorPosition++;
                    continue;
                }

                int index = allLines.indexOf(line);
                if (index != -1) {
                    allLines.remove(index);
                    allLines.add(index, line = this.replaceLineVariables(line, allLines));
                }

                commandsPerLine.put(line, new AbstractMap.SimpleEntry<>(cursorPosition, command));
            } catch (final IllegalArgumentException ex) {
                System.out.println("Unable to handle script line " + cursorPosition + ": " + line);
                ex.printStackTrace();
            }

            cursorPosition++;
        }

        Collection<IScriptTask> tasks = this.parseTasks(taskLines);
        if (commandsPerLine.isEmpty() && tasks.isEmpty()) {
            return null;
        }

        return new DefaultScript(path, this, allLines, tasks, commandsPerLine);
    }

    @Nullable
    private IScriptCommand getCommandOfLine(@NotNull String line) throws IllegalArgumentException {
        String[] arguments = line.split(" ");
        if (arguments.length == 0) {
            return null;
        }

        IScriptCommand result = this.getCommand(arguments[0]);
        if (result == null) {
            throw new IllegalArgumentException("Unable to find command by name " + arguments[0]);
        }

        return result;
    }

    @NotNull
    private String replaceLineVariables(@NotNull String line, @NotNull Collection<String> allLines) {
        String[] arguments = line.split(" ");
        if (arguments.length <= 1) {
            return line;
        }

        arguments = Arrays.copyOfRange(arguments, 1, arguments.length);
        for (String argument : arguments) {
            /*
            InterpreterVariable variable = this.getVariable(argument);
            if (variable == null) {
                continue;
            }

            line = line.replace(argument, variable.unwrap(line, allLines));*/ // TODO: 18.08.2022 variables
        }

        return line;
    }

    @NotNull
    private Collection<IScriptTask> parseTasks(@NotNull Collection<String> allLines) throws IllegalArgumentException {
        Collection<IScriptTask> tasks = new ArrayList<>();

        List<String> task = new CopyOnWriteArrayList<>();
        for (String line : allLines) {
            if (line.startsWith(TASK_END)) {
                tasks.add(this.parseTask(task, allLines));
                task.clear();
                continue;
            }

            if (line.startsWith(COMMENT_LINE_START) || line.trim().isEmpty()) {
                continue;
            }

            task.add(line);
        }

        if (!task.isEmpty()) {
            throw new IllegalArgumentException("Unclosed task: " + String.join("\n", task));
        }
        return tasks;
    }

    @Nullable
    private IScriptTask parseTask(@NotNull List<String> taskLines, @NotNull Collection<String> allLines) {
        if (taskLines.isEmpty()) {
            return null;
        }

        String taskOpener = taskLines.remove(0);
        String name = taskOpener.replaceFirst(TASK_LINE_START, "").replace("->", "").replace("{", "").trim();

        Map<String, IScriptCommand> commandsPerLine = new LinkedHashMap<>();

        for (String taskLine : taskLines) {
            taskLine = taskLine.trim();
            try {
                IScriptCommand command = this.getCommandOfLine(taskLine);
                if (command == null) {
                    continue;
                }

                int index = taskLines.indexOf(taskLine);
                if (index != -1) {
                    taskLines.remove(index);
                    taskLines.add(index, taskLine = this.replaceLineVariables(taskLine, allLines));
                }

                commandsPerLine.put(taskLine, command);
            } catch (final IllegalArgumentException ex) {
                System.out.println("Unable to handle script line " + taskLine);
                ex.printStackTrace();
            }
        }

        loadsTask = false;
        return new DefaultScriptTask(name, commandsPerLine);
    }
}
