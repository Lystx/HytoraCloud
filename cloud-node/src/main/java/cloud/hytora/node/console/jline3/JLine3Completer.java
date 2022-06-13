package cloud.hytora.node.console.jline3;

import cloud.hytora.common.misc.ReflectionUtils;
import cloud.hytora.driver.CloudDriver;
import cloud.hytora.driver.setup.Setup;
import cloud.hytora.driver.setup.SetupEntry;
import cloud.hytora.driver.setup.SetupSuggester;
import cloud.hytora.node.NodeDriver;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.Collection;
import java.util.List;

public class JLine3Completer implements Completer {

	@Override
	public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
		String buffer = line.line();
		Setup<?> currentSetup = NodeDriver.getInstance().getConsole().getCurrentSetup();
		if (currentSetup != null) {

			SetupEntry value = currentSetup.getSetup().getValue();
			if (value.getCompleter() != null) {
				Class<? extends SetupSuggester> value1 = value.getCompleter().value();
				SetupSuggester completer = ReflectionUtils.createEmpty(value1);
				for (String s : completer.suggest(currentSetup, currentSetup.getSetup().getValue())) {
					candidates.add(new Candidate(s));
				}
			}
			return;
		}

		Collection<String> responses = CloudDriver.getInstance().getCommandManager().completeCommand(CloudDriver.getInstance().getCommandSender(), buffer);

		for (String response : responses) {
			candidates.add(new Candidate(response));
		}
	}
}
