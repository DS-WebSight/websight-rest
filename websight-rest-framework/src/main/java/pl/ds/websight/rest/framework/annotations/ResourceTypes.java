package pl.ds.websight.rest.framework.annotations;

import org.osgi.service.component.annotations.ComponentPropertyType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use together with {@link pl.ds.websight.rest.framework.annotations.SlingAction} to register action using resourceTypes.
 */
@Retention(RetentionPolicy.RUNTIME)
@ComponentPropertyType
public @interface ResourceTypes {

    String[] value() default {};
}
