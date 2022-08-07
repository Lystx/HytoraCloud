package cloud.hytora.modules.proxy.config;

import cloud.hytora.common.DriverVersion;
import cloud.hytora.modules.proxy.config.sub.Motd;
import cloud.hytora.modules.proxy.config.sub.MotdLayOut;
import cloud.hytora.modules.proxy.config.sub.TabList;
import cloud.hytora.modules.proxy.config.sub.TabListFrame;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Collections;

@Getter @NoArgsConstructor @AllArgsConstructor
public class ProxyConfig {

	/**
	 * The tab list config
	 */
	private TabList tablist;

	/**
	 * The motd config
	 */
	private Motd motd;


	public static ProxyConfig defaultConfig() {

		DriverVersion version = DriverVersion.getCurrentVersion();

		return new ProxyConfig(
				new TabList(
						Collections.singletonList(new TabListFrame(
								String.join("\n", Arrays.asList(
										"&8",
										"&8        &8» &bHytoraCloud &8«        &8",
										"&8        &3Server &8» &7{service}     &8",
										"&8        &3Proxy &8» &7{proxy}        &8",
										"&8"
								)),
								String.join("\n", Arrays.asList(
										"&8",
										"&8 &3Twitter &8» &7@HytoraCloud    &8┃ &3Developer &8» &7Lystx     &8",
										"&8 &3Online &8» &7{players.online} &8┃ &3Max &8» &7{players.max} &8",
										"&8"
								))
						)),
						1
				),
				new Motd(
						Collections.singletonList(
								new MotdLayOut(
										"&8» &bHytoraCloud &8&l‴&7&l‴ &7your &bcloudSystem &8[&f1.8&7-&f1.18&8]",
										"&8» &3Status &8× §aOnline §8┃ §7Proxy &8× §3{proxy}",
										null,
										Collections.emptyList()
								)
						),
						Collections.singletonList(
								new MotdLayOut(
										"&8» &bHytoraCloud &8&l‴&7&l‴ &7your &bcloudSystem &8[&f1.8&7-&f1.18&8]",
										"&8» &3Status &8× §cMaintenance §8┃ §7Proxy &8× §3{proxy}",
										"&8» &c&oMaintenance",
										Arrays.asList(
												"§bHytoraCloud §7Information",
												"§8§m--------------------------",
												"§8",
												"&bVersion &8» &7" + version.toString(),
												"&bTwitter &8» &7@HytoraCloud",
												"&bDiscord &8» &7pazzqaGSVs",
												"§8",
												"§8§m--------------------------",
												"§8"
										)
								)
						)
				)
		);
	}
}
