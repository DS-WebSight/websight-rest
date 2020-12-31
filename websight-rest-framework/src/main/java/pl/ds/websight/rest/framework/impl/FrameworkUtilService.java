package pl.ds.websight.rest.framework.impl;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.component.annotations.Component;

@Component(service = FrameworkUtilService.class)
public class FrameworkUtilService {

    public Bundle getBundle(Class<?> clazz) {
        return FrameworkUtil.getBundle(clazz);
    }
}
