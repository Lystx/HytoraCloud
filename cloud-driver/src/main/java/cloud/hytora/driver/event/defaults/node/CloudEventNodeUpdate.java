package cloud.hytora.driver.event.defaults.node;

import cloud.hytora.driver.entity.node.INode;

public class CloudEventNodeUpdate extends AbstractNodeEvent {

    public CloudEventNodeUpdate(INode node) {
        super(node);
    }

}
