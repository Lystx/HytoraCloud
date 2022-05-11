package cloud.hytora.common.logging;

import lombok.AllArgsConstructor;

import javax.annotation.Nonnull;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;


@AllArgsConstructor
public class LogOutputStream extends ByteArrayOutputStream {

	private final Logger logger;
	private final LogLevel level;

	@Override
	public void flush() throws IOException {
		String input = this.toString(StandardCharsets.UTF_8.name());
		this.reset();

		if (input != null && !input.isEmpty() && !input.equals(System.lineSeparator())) {
			logger.log(level, input);
		}
	}

}
