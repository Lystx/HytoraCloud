package cloud.hytora.http.api;

import javax.annotation.Nonnull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpEndpoint {

	@Nonnull
	HttpMethod[] method();

	@Nonnull
	String path() default "";

	@Nonnull
	String permission() default "";

}
