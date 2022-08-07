package cloud.hytora.common.collection;

import java.util.Random;


public final class SingletonRandom extends RandomWrapper {

	public static final SingletonRandom INSTANCE = new SingletonRandom();

	private SingletonRandom() {
		super(new Random());
	}

}
