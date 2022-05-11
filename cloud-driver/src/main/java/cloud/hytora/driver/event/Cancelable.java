package cloud.hytora.driver.event;


public interface Cancelable {

	boolean isCancelled();

	void setCancelled(boolean cancelled);

}
