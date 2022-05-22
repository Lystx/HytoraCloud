package cloud.hytora.driver.http.impl;

import cloud.hytora.driver.http.SSLConfiguration;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.nio.file.Files;


public abstract class NettySSLServer {

	protected final SSLConfiguration sslConfiguration;

	public NettySSLServer(@Nullable SSLConfiguration sslConfiguration) {
		this.sslConfiguration = sslConfiguration;
	}

	protected SslContext sslContext;

	protected void initSslContext() throws Exception {
		if (sslConfiguration != null && sslConfiguration.isEnabled()) {
			if (sslConfiguration.getCertificatePath() != null && sslConfiguration.getPrivateKeyPath() != null) {

				try (InputStream certificate = Files.newInputStream(sslConfiguration.getCertificatePath());
				     InputStream privateKey = Files.newInputStream(sslConfiguration.getPrivateKeyPath());
				) {
					sslContext = SslContextBuilder.forServer(certificate, privateKey)
						.clientAuth(sslConfiguration.isClientAuth() ? ClientAuth.REQUIRE : ClientAuth.OPTIONAL)
						.trustManager(InsecureTrustManagerFactory.INSTANCE)
						.build();
				}

			} else {

				SelfSignedCertificate selfSignedCertificate = new SelfSignedCertificate();
				sslContext = SslContextBuilder.forServer(selfSignedCertificate.certificate(), selfSignedCertificate.privateKey())
					.clientAuth(sslConfiguration.isClientAuth() ? ClientAuth.REQUIRE : ClientAuth.OPTIONAL)
					.trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();

			}

		}
	}

	@Nullable
	public SslContext getSslContext() {
		return sslContext;
	}
}
