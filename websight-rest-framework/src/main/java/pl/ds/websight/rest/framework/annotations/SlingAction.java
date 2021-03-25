package pl.ds.websight.rest.framework.annotations;

import org.osgi.service.component.annotations.ComponentPropertyType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * <p>Use to annotate WebSight REST action classes.</p>
 * <p>See example actions in test sources package: {@code pl.ds.websight.rest.framework.impl}</p>
 * <p>REST action class:</p>
 * <ul>
 *     <li>name must end with RestAction</li>
 *     <li>must be annotated with {@link org.osgi.service.component.annotations.Component}</li>
 *     <li>must implement {@link pl.ds.websight.rest.framework.RestAction} generic interface with REST Action Model type</li>
 * </ul>
 * <p>REST Action Model class:</p>
 * <ul>
 *     <li>name must end with RestModel</li>
 *     <li>
 *         must be annotated with {@link org.apache.sling.models.annotations.Model} and adaptable from type
 *         {@link org.apache.sling.api.SlingHttpServletRequest}
 *         like: <code>@Model(adaptables = SlingHttpServletRequest.class)</code>
 *     </li>
 *     <li>fields can be annotated with {@link javax.validation.constraints} annotations to validate model (see lifecycle below)</li>
 *     <li>
 *         REST Action Model can optionally implements {@link pl.ds.websight.rest.framework.Validatable}
 *         to provide additional validation (see lifecycle below)
 *     </li>
 *     <li>
 *         must be listed in bundle header Sling-Model-Classes or Sling-Model-Packages (model package)
 *         to make Model usable (adaptable to) on Sling runtime (can be set via {@code bnd.bnd} file)
 *     </li>
 * </ul>
 * <p>
 * Model lifecycle:
 * <ul>
 *     <li>request is adapted to REST Action Model class</li>
 *     <li>
 *         REST Action Model class is validated according to {@link javax.validation.constraints} annotations;
 *         in case of validation failure response is returned with VALIDATION_FAILURE status and response entity field contains info about errors
 *     </li>
 *     <li>
 *         if REST Action Model implements {@link pl.ds.websight.rest.framework.Validatable} then
 *         {@link pl.ds.websight.rest.framework.Validatable#validate()} method is executed;
 *         if returned {@link pl.ds.websight.rest.framework.Errors} are not empty response is returned
 *         with VALIDATION_FAILURE status and response entity field contains info about errors
 *     </li>
 *     <li>Model is passed to {@link pl.ds.websight.rest.framework.RestAction#perform(Object)} and returned response is sent to client</li>
 *     <li>in case of {@link RuntimeException} during processing response with status ERROR is returned</li>
 * </ul>
 */
@Retention(RetentionPolicy.RUNTIME)
@ComponentPropertyType
public @interface SlingAction {

    HttpMethod value() default HttpMethod.POST;

    enum HttpMethod {
        GET, POST;
    }
}
