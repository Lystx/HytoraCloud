package cloud.hytora.driver.http.api;

import javax.annotation.Nonnull;


public enum HttpVersion {

	HTTP_1_0("HTTP/1.0"),
	HTTP_1_1("HTTP/1.1");

	private final String value;

	HttpVersion(@Nonnull String value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value;
	}
}
