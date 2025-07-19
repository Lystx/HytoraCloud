package cloud.hytora.driver.event.defaults.node;

import cloud.hytora.driver.entity.node.INode;

public class CloudEventNodeRegister extends AbstractNodeEvent {

    public CloudEventNodeRegister(INode node) {
        super(node);
    }

}
