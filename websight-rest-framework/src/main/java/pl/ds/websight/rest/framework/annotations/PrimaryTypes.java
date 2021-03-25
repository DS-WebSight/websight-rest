package pl.ds.websight.rest.framework.annotations;

import org.osgi.service.component.annotations.ComponentPropertyType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Use together with {@link pl.ds.websight.rest.framework.annotations.SlingAction} to register action for all resources
 * (sling/servlet/default) and validate requested resource against primaryType. Please note that servlet will return 400
 * Bad Request status code when action is run on node that's nodeType does not math any of provided primary types.
 */
@Retention(RetentionPolicy.RUNTIME)
@ComponentPropertyType
public @interface PrimaryTypes {

    String[] value() default {};
}
