package cloud.hytora.driver.entity.services.deployment;

import cloud.hytora.driver.networking.protocol.codec.buf.IBufferObject;
import cloud.hytora.driver.entity.services.template.ServiceTemplate;

import java.util.ArrayList;
import java.util.Collection;

public interface ServiceDeployment extends IBufferObject {



    static ServiceDeployment forInclude(ServiceTemplate template, Collection<String> onlyIncludeFiles) {
        return new CloudDeployment(template, new ArrayList<>(), onlyIncludeFiles);
    }

    static ServiceDeployment forExclude(ServiceTemplate template, Collection<String> exclude) {
        return new CloudDeployment(template, exclude, new ArrayList<>());
    }


    static ServiceDeployment forAll(ServiceTemplate template) {
        return new CloudDeployment(template, new ArrayList<>(), new ArrayList<>());
    }

    ServiceTemplate getTemplate();

    Collection<String> getOnlyIncludedFiles();

    Collection<String> getExclusionFiles();
}
