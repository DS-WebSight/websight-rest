package pl.ds.websight.rest.framework.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.Optional;
import org.apache.sling.models.annotations.Via;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;

import javax.inject.Inject;

@Model(adaptables = SlingHttpServletRequest.class)
public class HelloResourceTypePostModel {

    @Optional
    @Inject
    @Via("resource")
    private String name;

    @SlingObject
    private Resource resource;

    public String getName() {
        return name;
    }

    public Resource getResource() {
        return resource;
    }
}
