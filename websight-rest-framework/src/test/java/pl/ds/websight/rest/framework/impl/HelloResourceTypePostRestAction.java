package pl.ds.websight.rest.framework.impl;

import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.ResourceTypes;
import pl.ds.websight.rest.framework.annotations.SlingAction;

@Component
@SlingAction
@ResourceTypes("some/example/resource/type")
public class HelloResourceTypePostRestAction implements RestAction<HelloResourceTypePostModel, Void> {

    private static final Logger LOG = LoggerFactory.getLogger(HelloResourceTypePostRestAction.class);

    @Override
    public RestActionResult<Void> perform(HelloResourceTypePostModel model) {
        String messageDetails = "Hello World!";
        if (model.getName() != null) {
            messageDetails = "Hello " + model.getName() + "!";
        }
        ModifiableValueMap resourceProperties = model.getResource().adaptTo(ModifiableValueMap.class);
        if (resourceProperties != null) {
            resourceProperties.put("hello", messageDetails);
            try {
                model.getResource().getResourceResolver().commit();
                return RestActionResult.success("Greetings saved", messageDetails);
            } catch (PersistenceException e) {
                LOG.warn("Error during saving greetings", e);
                return RestActionResult.failure("Error during saving greetings", e.getMessage());
            }
        } else {
            return RestActionResult.failure("Cannot save greetings", "Resource " + model.getResource().getPath() + " cannot be modified");
        }
    }
}
