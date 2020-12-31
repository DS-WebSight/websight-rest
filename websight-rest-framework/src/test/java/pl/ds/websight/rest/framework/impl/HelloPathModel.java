package pl.ds.websight.rest.framework.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import pl.ds.websight.request.parameters.support.annotations.RequestParameter;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Validatable;

import javax.validation.constraints.NotNull;

@Model(adaptables = SlingHttpServletRequest.class)
public class HelloPathModel implements Validatable {

    @NotNull
    @RequestParameter
    private String name;

    @Override
    public Errors validate() {
        Errors errors = Errors.createErrors();
        if (Character.isLowerCase(name.charAt(0))) {
            errors.add("name", name, "Name should not start lower case");
        }
        return errors;
    }

    public String getName() {
        return name;
    }
}
