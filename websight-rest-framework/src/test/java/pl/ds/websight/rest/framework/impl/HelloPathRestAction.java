package pl.ds.websight.rest.framework.impl;

import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;

import static pl.ds.websight.rest.framework.annotations.SlingAction.HttpMethod.GET;

@Component
@SlingAction(GET)
public class HelloPathRestAction implements RestAction<HelloPathModel, Void> {

    @Override
    public RestActionResult<Void> perform(HelloPathModel model) {
        String message = "Hello " + model.getName() + "!";
        String messageDetails = "Your name is " + model.getName();
        return RestActionResult.success(message, messageDetails);
    }
}
