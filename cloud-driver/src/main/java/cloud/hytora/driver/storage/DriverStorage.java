package cloud.hytora.driver.storage;



import cloud.hytora.document.Document;

import javax.annotation.Nonnull;


public interface DriverStorage {

	@Nonnull
	Document getRawData();

	@Nonnull
	DriverStorage setRawData(@Nonnull Document rawData);

	@Nonnull
	default DriverStorage set(@Nonnull String path, @Nonnull Object value) {
		getRawData().set(path, value);
		return this;
	}

	default <T> T get(@Nonnull String path, @Nonnull Class<T> classOfT) {
		return getRawData().getInstance(path, classOfT);
	}

	/**
	 * Synchronizes all global config instances with the properties of this instance
	 */
	void update();

	/**
	 * Fetches the data
	 */
	void fetch();
}
