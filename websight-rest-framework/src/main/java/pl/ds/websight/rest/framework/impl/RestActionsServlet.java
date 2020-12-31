package pl.ds.websight.rest.framework.impl;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.OptingServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.Errors.Error;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.Validatable;
import pl.ds.websight.rest.framework.annotations.PrimaryTypes;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.IOException;

public class RestActionsServlet extends SlingAllMethodsServlet implements OptingServlet {

    private static final Logger LOG = LoggerFactory.getLogger(RestActionsServlet.class);

    private final transient RestAction restAction;
    private final transient Class<?> restActionModelType;
    private final transient ModelFactory modelFactory;
    private final transient Validator validator;
    private final transient RestResponseHandler restResponseHandler;
    private transient String[] primaryTypes;

    public RestActionsServlet(RestAction<?, ?> restAction, Class<?> restActionModelType, ModelFactory modelFactory,
            Validator validator, RestResponseHandler restResponseHandler) {
        this.restAction = restAction;
        this.restActionModelType = restActionModelType;
        this.modelFactory = modelFactory;
        this.validator = validator;
        this.restResponseHandler = restResponseHandler;
        PrimaryTypes primaryTypesAnnotation = restAction.getClass().getAnnotation(PrimaryTypes.class);
        if (primaryTypesAnnotation != null) {
            this.primaryTypes = primaryTypesAnnotation.value();
        }
    }

    @Override
    public boolean accepts(@NotNull SlingHttpServletRequest request) {
        return matchesPrimaryTypes(request.getResource());
    }

    @Override
    protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    @Override
    protected void doPost(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response) throws IOException {
        handle(request, response);
    }

    private void handle(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        try {
            Object model = null;
            Errors errors = null;
            if (!Void.class.equals(restActionModelType)) {
                model = modelFactory.createModel(request, restActionModelType);
                errors = getValidationErrors(model);
                if (errors.isEmpty() && model instanceof Validatable) {
                    errors = ((Validatable) model).validate();
                }
            }
            if (errors == null || errors.isEmpty()) {
                RestActionResult result = restAction.perform(model);
                restResponseHandler.handle(request, response, result);
            } else {
                restResponseHandler.handleModelValidationError(request, response, errors);
            }
        } catch (RuntimeException e) {
            LOG.error("Unable to perform rest action {}", restAction.getClass().getName(), e);
            restResponseHandler.handleRuntimeException(request, response, e);
        }
    }

    private boolean matchesPrimaryTypes(Resource resource) {
        if (ArrayUtils.isEmpty(primaryTypes)) {
            return true;
        } else {
            Node node = resource.adaptTo(Node.class);
            if (node != null) {
                for (String primaryType : primaryTypes) {
                    try {
                        if (node.isNodeType(primaryType)) {
                            return true;
                        }
                    } catch (RepositoryException e) {
                        LOG.warn("Failed to check node's primaryType", e);
                        return false;
                    }
                }
            } else {
                LOG.warn("Cannot adapt resource {} to JCR node", resource.getPath());
            }
        }
        return false;
    }

    private Errors getValidationErrors(Object model) {
        return Errors.of(validator.validate(model).stream().map(this::errorOf).toArray(Error[]::new));
    }

    private Error errorOf(ConstraintViolation<Object> violation) {
        return Error.of(
                violation.getPropertyPath().toString(),
                violation.getInvalidValue(),
                violation.getMessage());
    }
}
