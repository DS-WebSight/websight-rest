package pl.ds.websight.rest.framework.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Via;

import javax.inject.Inject;

@Model(adaptables = SlingHttpServletRequest.class)
public class HelloPrimaryTypeModel {

    @Optional
    @Inject
    @Via("resource")
    private String name;

    public String getName() {
        return name;
    }
}
