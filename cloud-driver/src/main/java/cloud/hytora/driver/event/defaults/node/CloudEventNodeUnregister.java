package cloud.hytora.driver.event.defaults.node;

import cloud.hytora.driver.entity.node.INode;

public class CloudEventNodeUnregister extends AbstractNodeEvent {

    public CloudEventNodeUnregister(INode node) {
        super(node);
    }

}
