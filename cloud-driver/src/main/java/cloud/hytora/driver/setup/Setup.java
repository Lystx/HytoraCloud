package cloud.hytora.driver.setup;

import cloud.hytora.common.function.BiSupplier;
import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.console.screen.Screen;
import cloud.hytora.driver.console.screen.ScreenManager;
import cloud.hytora.driver.console.TabCompleter;
import cloud.hytora.driver.setup.annotations.*;
import cloud.hytora.driver.CloudDriver;
import com.google.gson.internal.Primitives;
import lombok.Getter;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This class is used for Setup purposes.
 * To create a Setup just make the class extend {@link Setup}
 *
 * @param <T> the generic setup type
 */
public abstract class Setup<T extends Setup<?>> {

    private static final Map<Class<?>, SetupInputParser<?>> inputTransformers = new HashMap<>();

    public static <T> void registerTransformer(Class<T> clazz, SetupInputParser<T> supplier) {
        inputTransformers.put(clazz, supplier);
    }

    static {

        registerTransformer(int.class, (entry, input) -> Integer.parseInt(input));
        registerTransformer(double.class, (entry, input) -> Double.parseDouble(input));
        registerTransformer(long.class, (entry, input) -> Long.parseLong(input));
        registerTransformer(byte.class, ((entry, input) -> Byte.parseByte(input)));
        registerTransformer(short.class, ((entry, input) -> Short.parseShort(input)));
        registerTransformer(float.class, (entry, input) -> Float.parseFloat(input));
        registerTransformer(boolean.class, (entry, input) -> Boolean.parseBoolean(input));

        registerTransformer(String.class, (entry, input) -> input);
        registerTransformer(Integer.class, (entry, input) -> Integer.parseInt(input));
        registerTransformer(Double.class, (entry, input) -> Double.parseDouble(input));
        registerTransformer(Long.class, (entry, input) -> Long.parseLong(input));
        registerTransformer(Byte.class, ((entry, input) -> Byte.parseByte(input)));
        registerTransformer(Short.class, ((entry, input) -> Short.parseShort(input)));
        registerTransformer(Float.class, (entry, input) -> Float.parseFloat(input));
        registerTransformer(Boolean.class, (entry, input) -> Boolean.parseBoolean(input));
        registerTransformer(Enum.class, (entry, input) -> {
            if (entry.getRequiresEnum() == null) {
                throw new IllegalStateException("To use an Enum in Setup you need the @RequiresEnum annotation!");
            }
            RequiresEnum requiresEnum = entry.getRequiresEnum();
            Class value = requiresEnum.value();

            return Enum.valueOf(value, input.trim().toUpperCase());
        });
    }

    /**
     * The setup parts
     */
    private final Map<Field, SetupEntry> map;

    /**
     * The current setup part
     */
    private int current;

    /**
     * If the setup is cancelled
     */
    @Getter
    private boolean cancelled;

    /**
     * If the setup was exited after one answer
     */
    @Getter
    private boolean exitAfterAnswer;

    /**
     * The current setup fields and their questions
     */
    @Getter
    private Map.Entry<Field, SetupEntry> setup;

    /**
     * The consumer when its finished
     */
    private SetupListener<T> setupListener;

    /**
     * To identify console screen
     */
    private final String uniqueSetupName;

    /**
     * If this setup is allowed to be cancelled
     */
    public abstract boolean isCancellable();

    /**
     * If a header should be printed
     */
    public abstract boolean shouldPrintHeader();

    public Setup() {

        this.cancelled = false;
        this.exitAfterAnswer = false;
        this.map = new HashMap<>();
        this.current = 1;

        this.loadSetupParts();
        this.uniqueSetupName = "setup#" + UUID.randomUUID();

        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ScreenManager.class)
                .registerScreen(uniqueSetupName, false);
    }

    public Screen getSetupScreen() {
        return CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class).getScreenByNameOrNull(this.uniqueSetupName);
    }

    public void start(SetupListener<T> finishHandler) {
        this.setupListener = finishHandler;


        CloudDriver.getInstance()
                .getProviderRegistry()
                .getUnchecked(ScreenManager.class)
                .getScreenByNameOrNull(this.uniqueSetupName)
                .registerTabCompleter(buffer -> {
                    SetupEntry value = getSetup().getValue();
                    if (value.getCompleter() != null) {
                        Class<? extends SetupSuggester> value1 = value.getCompleter().value();
                        SetupSuggester completer = ReflectionUtils.createEmpty(value1);
                        if (completer != null) {
                            return completer.suggest(Setup.this, getSetup().getValue());
                        }
                    }
                    return new ArrayList<>();
                })
                .registerInputHandler(input -> {

                    //While current id is in range of map-cache
                    if (this.current < this.map.size() + 1) {
                        //Reading input and executing Setup#next(String)
                        executeInput(input);
                    } else {
                        this.exit(true);
                    }
                }).join();  //joining setup screen

        //Setting current setup
        this.setup = this.getEntry(1);
        this.printQuestion(this.setup.getValue());
    }

    @SuppressWarnings("unchecked")
    private void exit(boolean success) {
        ScreenManager unchecked = CloudDriver.getInstance().getProviderRegistry().getUnchecked(ScreenManager.class);


        unchecked.leaveCurrentScreen();
        unchecked.unregisterScreen(this.uniqueSetupName);


        //If already exited by another code line
        if (this.setupListener != null) {
            //Setup done and accepting consumer
            if (success) {
                this.setupListener.accept((T) this, SetupControlState.FINISHED);
            } else {
                this.setupListener.accept((T) this, SetupControlState.CANCELLED);
            }
            this.setupListener = null;
        }

    }


    /**
     * Handles the current question with a given input
     * It checks if the answer should exit after or jump
     * to a other question and then set the current part higher
     * <p>
     * Checks for disallowed answers or only allowed answers
     *
     * @param input the input
     */
    public void executeInput(String input) {
        if (this.setup != null) {

            //No input provided
            if (input.trim().isEmpty()) {
                this.getSetupScreen().writeLine("§cPlease do not enter §eempty §cinput!");
                return;
            }

            //Cancelling setup
            if (input.equalsIgnoreCase("cancel")) {
                if (!this.isCancellable()) {
                    this.getSetupScreen().writeLine("§cYou cannot cancel the current setup§c!");
                    return;
                }
                this.getSetupScreen().writeLine("§cThe current setup was §ecancelled§c!");
                this.cancelled = true;
                this.current += 10000;

                this.exit(false);
                return;
            }

            SetupEntry setupEntry = this.setup.getValue();

            //If answer is enum only
            if (!setupEntry.isEnumRequired(input)) {
                this.getSetupScreen().writeLine("§cPossible answers: §e" + Arrays.toString(setup.getValue().getRequiresEnum().value().getEnumConstants()).replace("]", ")").replace("[", "("));
                return;
            }

            //If the current input is not allowed for this setup question because you provided a wrong type
            if (!setupEntry.isAllowed(input)) {

                String[] onlyAllowed = null;
                if (setupEntry.getAnswers() != null) {
                    onlyAllowed = setupEntry.getAnswers().only();
                }

                if (onlyAllowed == null || onlyAllowed.length == 0) {
                    this.getSetupScreen().writeLine("§cCouldn't show you any possible answers because no possible answers were provided in the Setup!");
                } else {
                    this.getSetupScreen().writeLine("§cPossible answers: §e" + Arrays.toString(onlyAllowed).replace("]", "").replace("[", ""));
                }
                this.getSetupScreen().writeLine("§cRequired Type: §e" + this.setup.getKey().getType().getSimpleName());
                return;
            }

            //If the current input is forbidden to use
            if (setupEntry.isForbidden(input)) {
                this.getSetupScreen().writeLine(!input.trim().isEmpty() ? ("§cThe answer '§e" + input + " §cmay not be used for this question!") : "§cThis §eanswer §cmay not be used for this question!");
                return;
            }

            ConditionChecker checker = setupEntry.getChecker();
            if (checker != null) {
                Class<? extends BiSupplier<String, Boolean>> value = checker.value();
                BiSupplier<String, Boolean> supplier = ReflectionUtils.createEmpty(value);
                if (supplier != null) {
                    if (supplier.supply(input)) {
                        this.getSetupScreen().writeLine(checker.message().replace("%input%", input));
                        return;
                    }
                }
            }

            //Accessing the setup field
            this.setup.getKey().setAccessible(true);
            try {
                Object value;
                Field field = this.setup.getKey();
                Class<?> type = Primitives.wrap(field.getType());

                if (Enum.class.isAssignableFrom(type)) {
                    type = Enum.class;
                }

                SetupInputParser<?> transformer = inputTransformers.get(type);
                value = transformer.parse(setupEntry, input);

                if (value == null) {
                    this.getSetupScreen().writeLine("§cPlease try again");
                    return;
                }

                //Setting setup value
                this.setup.getKey().set(this, value);

            } catch (Exception ex) {
                this.getSetupScreen().writeLine("§cThe §einput §cdidn't match any of the available §eAnswerTypes§c!");
                return;
            }

            //If the setup should exit after this answer
            if (setupEntry.isExitAfterInput(input)) {
                this.current += 10000;
                this.exitAfterAnswer = true;
                return;
            }
        }

        //Going to next question going +1
        this.current++;
        this.setup = this.getEntry(this.current);

        //Could be last question and setup is not found
        if (this.setup != null) {
            //Sending question again and waiting for input
            this.printQuestion(setup.getValue());
        } else {
            this.exit(true);
        }
    }

    /**
     * Get an entry from the map cache
     *
     * @param id the question id
     * @return entry
     */
    private Map.Entry<Field, SetupEntry> getEntry(int id) {
        Map.Entry<Field, SetupEntry> entry = null;
        for (Map.Entry<Field, SetupEntry> currentEntry : this.map.entrySet()) {
            if (currentEntry.getValue().getQuestion().id() == id) {
                entry = currentEntry;
            }
        }
        return entry;
    }

    /**
     * Loads all setup fields that are
     * annotated with {@link Question}
     */
    private void loadSetupParts() {
        //Caching the setup fields and parts
        for (Field field : getClass().getDeclaredFields()) {
            if (field.getAnnotation(Question.class) != null) {

                SetupEntry setupEntry = new SetupEntry(
                        field,
                        field.getAnnotation(Question.class),
                        field.getAnnotation(RequiresEnum.class),
                        field.getAnnotation(Answers.class),
                        field.getAnnotation(ExitAfterInput.class),
                        field.getAnnotation(SuggestedAnswer.class),
                        field.getAnnotation(QuestionTip.class),
                        field.getAnnotation(ConditionChecker.class),
                        field.getAnnotation(AnswerCompleter.class)
                );

                this.map.put(field, setupEntry);
            }
        }
    }

    private void printQuestion(SetupEntry entry) {
        if (this.shouldPrintHeader()) {
            this.printHeader(getClass().getSimpleName() + " at " + new SimpleDateFormat("HH:mm:ss").format(System.currentTimeMillis()));
        }

        if (entry.getSuggestedAnswer() != null) {
            String value = entry.getSuggestedAnswer().value();
            this.getSetupScreen().suggestInput(value);
        }

        if (entry.getRequiresEnum() != null) {
            List<String> collect = Arrays.stream(entry.getRequiresEnum().value().getEnumConstants()).map(Enum::name).collect(Collectors.toList());
            this.getSetupScreen().setHistory(collect);
        }

        if (entry.getAnswers() != null) {
            List<String> collect = Arrays.stream(entry.getAnswers().only()).collect(Collectors.toList());
            this.getSetupScreen().setHistory(collect);
        }

        //Sending first question without any input

        StringBuilder sb = new StringBuilder(entry.getQuestion().question() + (entry.getQuestionTip() == null ? "" : " (Tip: " + entry.getQuestionTip().value() + ")"));
        if (entry.getAnswers() != null) {
            sb.append(" ").append((Arrays.toString(entry.getAnswers().only())).replace("[", "(").replace("]", ")"));
        }
        this.getSetupScreen().writeLine(sb.toString());

        if (entry.getRequiresEnum() != null) {
            this.getSetupScreen().writeLine("§7Possible Answers§8: §b" + Arrays.toString(entry.getRequiresEnum().value().getEnumConstants()).replace("]", "§8)").replace("[", "§8(§b").replace(",", "§8, §b"));
        }


    }


    /**
     * Prints the header with its information
     * <p>
     * > If its cancellable
     * > Current Question ID
     * > Setup-Name
     */
    private void printHeader(String header) {
        this.getSetupScreen().clear();

        this.getSetupScreen().writeLine("§8");
        this.getSetupScreen().writeLine(header);
        this.getSetupScreen().writeLine("§8");
        if (this.isCancellable()) {
            this.getSetupScreen().writeLine("§7» §7You can cancel this setup by typing \"§ecancel§7\"!");
        } else {
            this.getSetupScreen().writeLine("§7» §7This setup is §cnot allowed §7to be cancelled!");
        }
        this.getSetupScreen().writeLine("§7» §7Suggested answers can be §coverridden §7by typing your own!");
        this.getSetupScreen().writeLine("§7» §7Suggested answers can be accepted by hitting §aenter§7!");
        this.getSetupScreen().writeLine("§7» §7Hit §eTAB §7to see possible answers§7!");
        this.getSetupScreen().writeLine("§7» §7Current Question §f: §b" + (this.current == 1 ? 1 : current) + "/" + (this.map.keySet().size() == 0 ? "Loading" : this.map.keySet().size() + ""));
        this.getSetupScreen().writeLine("§8");
    }

}
