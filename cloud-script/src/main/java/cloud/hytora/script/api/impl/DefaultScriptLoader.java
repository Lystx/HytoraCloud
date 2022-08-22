package cloud.hytora.script.api.impl;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.logging.Logger;
import cloud.hytora.script.ScriptSyntax;
import cloud.hytora.script.api.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;


public class DefaultScriptLoader implements IScriptLoader {



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
            return this.loadScript(script, allLines);
        } catch (final IOException ex) {
            Logger.constantInstance().error("Unable to read  script located at {} : {}", script.toString(), ex);
        }

        return null;
    }

    boolean loadsTask = false;
    boolean multiComment = false;
    boolean loadDecision, loadsFalseDecision = false;
    DefaultScriptDecision currentDecision = null;

    @Nullable
    private IScript loadScript(@NotNull Path path, @NotNull List<String> allLines) {
        Map<String, Map.Entry<Integer, IScriptCommand>> commandsPerLine = new LinkedHashMap<>();
        Map<String, Map.Entry<Integer, IScriptDecision>> decisionsPerLine = new LinkedHashMap<>();
        Map<Integer, IScriptDecision> decisions = new HashMap<>();
        int cursorPosition = 0;
        List<String> taskLines = new ArrayList<>();

        for (String line : allLines) {
            if (line.trim().startsWith(ScriptSyntax.COMMENT_LINE_START) || line.trim().isEmpty()) {
                cursorPosition++;
                continue;
            }


            //start ignoring lines
            if (line.trim().startsWith(ScriptSyntax.MULTI_COMMENT_LINE_START)) {
                multiComment = true;
                cursorPosition++;
                continue;
            }

            //end ignoring lines
            if (line.trim().startsWith(ScriptSyntax.MULTI_COMMENT_LINE_END) || line.trim().endsWith(ScriptSyntax.MULTI_COMMENT_LINE_END)) {
                multiComment = false;
                cursorPosition++;
                continue;
            }

            //check if ignoring lines
            if (multiComment) {
                cursorPosition++;
                continue;
            }

            if (line.trim().startsWith("if")) {
                if (line.endsWith("-> {")) {
                    loadDecision = true;
                    String checker = line
                            .replace("if", "")
                            .replace("(", "")
                            .replace(")", "")
                            .replace("->", "")
                            .replace("{", "")
                            .trim();
                    currentDecision = new DefaultScriptDecision();
                    currentDecision.setChecker(script -> {
                        return Boolean.parseBoolean(script.replaceVariables(checker).trim());
                    });
                    cursorPosition++;
                    continue;
                } else {
                    throw new IllegalArgumentException("If-Action must be closed with ' -> {' !");
                }
            }

            //decision request over and closed
            if (line.trim().equalsIgnoreCase("}") && (loadDecision || loadsFalseDecision)) {
                loadDecision = false;
                loadsFalseDecision = false;
                decisionsPerLine.put(line.trim(), new AbstractMap.SimpleEntry<>(cursorPosition, currentDecision));
                currentDecision = null;

                cursorPosition++;
                continue;
            }

            //decision request over and closed
            if (line.trim().equalsIgnoreCase("} else -> {")) {
                loadDecision = false;
                loadsFalseDecision = true;
                cursorPosition++;
                continue;
            }

            if (loadDecision) {
                IScriptCommand command = this.getCommandOfLine(line.trim());
                if (command != null) {
                    currentDecision.getTrueCommands().put(line.trim(), command);
                }

                cursorPosition++;
                continue;
            }

            if (loadsFalseDecision) {
                IScriptCommand command = this.getCommandOfLine(line.trim());
                if (command != null) {
                    currentDecision.getFalseCommands().put(line.trim(), command);
                }

                cursorPosition++;
                continue;
            }

            //start loading task
            if (line.startsWith(ScriptSyntax.TASK_LINE_START)) {
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

        return new DefaultScript(path, this, allLines, tasks, commandsPerLine, decisionsPerLine);
    }

    @Nullable
    private IScriptCommand getCommandOfLine(@NotNull String line) throws IllegalArgumentException {
        String[] arguments = line.split(" ");
        if (arguments.length == 0) {
            return null;
        }

        IScriptCommand result = this.getCommand(arguments[0]);
        if (result == null) {
            throw new IllegalArgumentException("Unable to find command by name " + arguments[0] + " [Line: '" + line + "]");
        }

        return result;
    }

    @NotNull
    private String replaceLineVariables(@NotNull String line, @NotNull Collection<String> allLines) {
        return line;
    }

    @NotNull
    private Collection<IScriptTask> parseTasks(@NotNull Collection<String> allLines) throws IllegalArgumentException {
        Collection<IScriptTask> tasks = new ArrayList<>();

        List<String> task = new CopyOnWriteArrayList<>();
        for (String line : allLines) {
            if (line.startsWith(ScriptSyntax.TASK_END)) {
                loadsTask = false;
                tasks.add(this.parseTask(task, allLines));
                task.clear();
                continue;
            }

            if (line.trim().startsWith(ScriptSyntax.COMMENT_LINE_START) || line.trim().isEmpty()) {
                continue;
            }


            //start ignoring lines
            if (line.startsWith(ScriptSyntax.MULTI_COMMENT_LINE_START)) {
                multiComment = true;
                continue;
            }

            //end ignoring lines
            if (line.startsWith(ScriptSyntax.MULTI_COMMENT_LINE_END) || line.endsWith(ScriptSyntax.MULTI_COMMENT_LINE_END)) {
                multiComment = false;
                continue;
            }

            //check if ignoring lines
            if (multiComment) {
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
        String name = taskOpener.replaceFirst(ScriptSyntax.TASK_LINE_START, "").replace("->", "").replace("{", "").trim();

        Map<String, IScriptCommand> commandsPerLine = new LinkedHashMap<>();
        Map<String, IScriptDecision> decisionsPerLine = new LinkedHashMap<>();

        for (String taskLine : taskLines) {
            taskLine = taskLine.trim();


            if (taskLine.trim().startsWith("if")) {
                if (taskLine.endsWith("-> {")) {
                    loadDecision = true;
                    String checker = taskLine
                            .replace("if", "")
                            .replace("(", "")
                            .replace(")", "")
                            .replace("->", "")
                            .replace("{", "")
                            .trim();
                    currentDecision = new DefaultScriptDecision();
                    currentDecision.setChecker(script -> {
                        return Boolean.parseBoolean(script.replaceVariables(checker).trim());
                    });
                    continue;
                } else {
                    throw new IllegalArgumentException("If-Action must be closed with ' -> {' !");
                }
            }

            //decision request over and closed
            if (taskLine.trim().equalsIgnoreCase("}") && (loadDecision || loadsFalseDecision)) {
                loadDecision = false;
                loadsFalseDecision = false;
                decisionsPerLine.put(taskLine.trim(), currentDecision);
                currentDecision = null;
                continue;
            }

            //decision request over and closed
            if (taskLine.trim().equalsIgnoreCase("} else -> {")) {
                loadDecision = false;
                loadsFalseDecision = true;
                continue;
            }

            if (loadDecision) {
                IScriptCommand command = this.getCommandOfLine(taskLine.trim());
                if (command != null) {
                    currentDecision.getTrueCommands().put(taskLine.trim(), command);
                }
                continue;
            }

            if (loadsFalseDecision) {
                IScriptCommand command = this.getCommandOfLine(taskLine.trim());
                if (command != null) {
                    currentDecision.getFalseCommands().put(taskLine.trim(), command);
                }
                continue;
            }

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
        return new DefaultScriptTask(name, commandsPerLine, decisionsPerLine);
    }
}
