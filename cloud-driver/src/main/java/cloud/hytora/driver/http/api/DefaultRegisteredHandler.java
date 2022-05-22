package cloud.hytora.driver.http.api;

import lombok.AllArgsConstructor;
import lombok.Getter;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;

@AllArgsConstructor
@Getter
public class DefaultRegisteredHandler implements RegisteredHandler {

	private final Object holder;
	private final Method method;

	private final String path;
	private final String permission;
	private final HttpMethod[] methods;

	@Override
	public void execute(@Nonnull HttpContext context) throws Exception {
		method.invoke(holder, context);
	}

}
