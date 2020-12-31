package pl.ds.websight.rest.framework.impl;

import com.google.common.base.CaseFormat;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.models.annotations.Model;
import org.osgi.framework.Bundle;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.annotations.PrimaryTypes;
import pl.ds.websight.rest.framework.annotations.ResourceTypes;
import pl.ds.websight.rest.framework.annotations.SlingAction;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Optional;

@Component(service = RestActionSetupService.class)
public class RestActionSetupService {

    private static final String REST_ACTION_CLASS_NAME_SUFFIX = "RestAction";
    private static final String ACTION_EXTENSION = "action";

    @Reference
    private FrameworkUtilService frameworkUtil;

    /**
     * Get model type from action class definition.
     *
     * @throws IllegalStateException if class definition is not correct
     */
    public Class<?> getModelClass(RestAction<?, ?> restAction) {
        Class<?> modelClass = getRestActionGenericType(restAction);
        if (Void.class.equals(modelClass)) {
            return modelClass;
        }
        if (isValidSlingModel(modelClass)) {
            return modelClass;
        }
        throw new IllegalStateException(
                String.format("Model type of action %s is not Sling model adaptable from request.", restAction.getClass()));
    }

    private Class<?> getRestActionGenericType(RestAction<?, ?> restAction) {
        Class<?> analyzedClass = restAction.getClass();
        // we can't use getInterfaces(), since it returns parameterized interfaces in raw form
        Type[] genericInterfaces = analyzedClass.getGenericInterfaces();
        while (genericInterfaces.length == 0 && RestAction.class.isAssignableFrom(analyzedClass.getSuperclass())) {
            analyzedClass = analyzedClass.getSuperclass();
            genericInterfaces = analyzedClass.getGenericInterfaces();
        }
        for (Type genericInterface : genericInterfaces) {
            if (genericInterface instanceof ParameterizedType) {
                Class<?> rawType = (Class<?>) ((ParameterizedType) genericInterface).getRawType();
                if (rawType.isAssignableFrom(RestAction.class)) {
                    Type[] genericTypes = ((ParameterizedType) genericInterface).getActualTypeArguments();
                    if (genericTypes.length > 0) {
                        return (Class<?>) genericTypes[0];
                    }
                }
            }
        }
        throw new IllegalStateException("Cannot get model type from action " + restAction.getClass());
    }

    private boolean isValidSlingModel(Class<?> modelClass) {
        return Optional.ofNullable(modelClass.getAnnotation(Model.class))
                .map(Model::adaptables)
                .map(Arrays::asList)
                .orElse(Collections.emptyList())
                .stream()
                .anyMatch(adaptable -> SlingHttpServletRequest.class == adaptable);
    }

    /**
     * Builds properties for action servlet from action class.
     *
     * @throws IllegalStateException if class definition is not correct
     */
    public Dictionary<String, Object> buildActionServletProperties(RestAction<?, ?> restAction) {
        SlingAction slingAction = restAction.getClass().getAnnotation(SlingAction.class);
        if (slingAction == null) {
            throw new IllegalStateException(String.format("No %s annotation at %s",
                    SlingAction.class.getSimpleName(), restAction.getClass()));
        }
        ResourceTypes resourceTypes = restAction.getClass().getAnnotation(ResourceTypes.class);
        PrimaryTypes primaryTypes = restAction.getClass().getAnnotation(PrimaryTypes.class);

        if (resourceTypes != null && primaryTypes != null) {
            throw new IllegalStateException("RestAction cannot be annotated with both PrimaryTypes " +
                    "and ResourceTypes annotations");
        }
        Dictionary<String, Object> properties = new Hashtable<>(); // NOSONAR
        if (primaryTypes != null && ArrayUtils.isNotEmpty(primaryTypes.value())) {
            properties.put(ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES, ServletResolverConstants.DEFAULT_RESOURCE_TYPE);
            properties.put(ServletResolverConstants.SLING_SERVLET_SELECTORS, buildSelectors(restAction));
        } else if (resourceTypes != null && ArrayUtils.isNotEmpty(resourceTypes.value())) {
            properties.put(ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES, resourceTypes.value());
            properties.put(ServletResolverConstants.SLING_SERVLET_SELECTORS, buildSelectors(restAction));
        } else {
            properties.put(ServletResolverConstants.SLING_SERVLET_PATHS, buildActionPath(restAction));
            properties.put("sling.servlet.paths.strict", Boolean.TRUE);
        }
        properties.put(ServletResolverConstants.SLING_SERVLET_EXTENSIONS, ACTION_EXTENSION);
        properties.put(ServletResolverConstants.SLING_SERVLET_METHODS, slingAction.value().toString());
        properties.put("websight.action.class", restAction.getClass().getName());
        return properties;
    }

    private String buildSelectors(RestAction<?, ?> restAction) {
        return StringUtils.joinWith(".", getBundleNameSymbolicName(restAction), buildActionName(restAction));
    }

    private String buildActionPath(RestAction<?, ?> restAction) {
        String bundleSymbolicName = getBundleNameSymbolicName(restAction);
        String actionName = buildActionName(restAction);
        return String.format("/apps/%s/bin/%s", bundleSymbolicName, actionName);
    }

    private String getBundleNameSymbolicName(RestAction<?, ?> restAction) {
        Bundle bundle = frameworkUtil.getBundle(restAction.getClass());
        if (bundle == null) {
            throw new IllegalStateException("No Bundle for " + restAction.getClass());
        }
        return bundle.getSymbolicName();
    }

    private String buildActionName(RestAction<?, ?> restAction) {
        String actionName = restAction.getClass().getSimpleName();
        if (!StringUtils.endsWith(actionName, REST_ACTION_CLASS_NAME_SUFFIX)) {
            throw new IllegalStateException(String.format("No %s suffix in class name %s",
                    REST_ACTION_CLASS_NAME_SUFFIX, restAction.getClass()));
        }
        actionName = StringUtils.removeEnd(actionName, REST_ACTION_CLASS_NAME_SUFFIX);
        if (StringUtils.isEmpty(actionName)) {
            throw new IllegalStateException(String.format("No action name before %s suffix in class name %s",
                    REST_ACTION_CLASS_NAME_SUFFIX, restAction.getClass()));
        }
        return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_HYPHEN, actionName);
    }
}
