package pl.ds.websight.rest.framework.impl;

import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.PrimaryTypes;
import pl.ds.websight.rest.framework.annotations.SlingAction;

@SlingAction
@PrimaryTypes("nt:folder")
public class HelloPrimaryTypeRestAction implements RestAction<HelloPrimaryTypeModel, Void> {

    @Override
    public RestActionResult<Void> perform(HelloPrimaryTypeModel model) {
        String message = "Hello " + model.getName() + "!";
        String messageDetails = "Your name is " + model.getName();
        return RestActionResult.success(message, messageDetails);
    }
}
