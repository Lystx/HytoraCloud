package cloud.hytora.driver.setup;

import cloud.hytora.common.function.ExceptionallyBiConsumer;

public interface SetupListener<T extends Setup<?>> extends ExceptionallyBiConsumer<T, SetupControlState> {


}
