package cloud.hytora.driver.script;

import cloud.hytora.common.misc.FileUtils;

import javax.annotation.Nonnull;
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


	public void registerCommand(String name, @Nonnull ScriptCommand command) {
		commands.put(name, command);
	}

	public void registerCommand(ScriptCommand command, String... names) {
		for (String name : names) {
			registerCommand(name, command);
		}
	}

	public void executeScript(@Nonnull Path path) throws IOException {
		executeScriptFromReader(FileUtils.newBufferedReader(path));
	}

	public void executeScriptFromInputStream(@Nonnull InputStream input) throws IOException {
		executeScriptFromReader(new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)));
	}

	public void executeScriptFromResource(String resource) throws IOException {
		InputStream systemResourceAsStream = ClassLoader.getSystemResourceAsStream(resource);
		assert systemResourceAsStream != null;
		this.executeScriptFromReader(new BufferedReader(new InputStreamReader(systemResourceAsStream, StandardCharsets.UTF_8)));
	}

	public void executeScriptFromReader(@Nonnull BufferedReader reader) throws IOException {
		String line;
		while ((line = reader.readLine()) != null) {
			executeLine(line);
		}
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

	private void executeLine(@Nonnull String line) {
		if (line.startsWith("#") || line.trim().isEmpty()) {
			//comments or empty line -> ignoring
			return;
		}

		//replacing line variables
		line = replaceScriptVariables(line);

		String[] args = line.split(" ");
		String name = args[0];

		if (args.length == 1) {
			args = new String[0];
		} else {
			args = Arrays.copyOfRange(args, 1, args.length);
		}

		ScriptCommand command = commands.get(name);
		if (command == null) {
			throw new IllegalStateException("Unsupported syntax '" + line + "'");
		}
		command.execute(args, String.join(" ", args), line);
	}
}
