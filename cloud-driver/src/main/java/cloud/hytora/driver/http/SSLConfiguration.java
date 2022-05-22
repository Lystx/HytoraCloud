package cloud.hytora.driver.http;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.nio.file.Path;
import java.nio.file.Paths;


@AllArgsConstructor @Getter
public class SSLConfiguration {

	/**
	 * If ssl should be enabled
	 */
	private final boolean enabled;

	/**
	 * If network-clients should auth aswell
	 */
	private final boolean clientAuth;

	/**
	 * The path leading to the certificate file
	 */
	private final String certificatePath;

	/**
	 * The path leading to the private key file
	 */
	private final String privateKeyPath;


	public Path getCertificatePath() {
		return Paths.get(certificatePath);
	}

	public Path getPrivateKeyPath() {
		return Paths.get(privateKeyPath);
	}
}
