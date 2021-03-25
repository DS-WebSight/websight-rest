package pl.ds.websight.rest.framework.impl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.net.HttpHeaders;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.Errors;
import pl.ds.websight.rest.framework.RestActionResult;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static pl.ds.websight.rest.framework.RestActionResult.Status;

@Component(service = RestResponseHandler.class)
public class RestResponseHandler {

    private static final Logger LOG = LoggerFactory.getLogger(RestResponseHandler.class);

    private static final String ERROR_STATUS_CODE = "ERROR";
    private static final String VALIDATION_FAILURE_STATUS_CODE = "VALIDATION_FAILURE";

    private static final ObjectWriter JSON_WRITER = new ObjectMapper()
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .writer();

    public void handle(SlingHttpServletRequest request, SlingHttpServletResponse response, RestActionResult restActionResult) throws IOException {
        if (Status.FREE_FORM_RESPONSE.equals(restActionResult.getStatus())) {
            handle(restActionResult.getResponseHandler(), response);
        } else {
            handle(request, response, new ResultJsonModel(restActionResult), HttpServletResponse.SC_OK);
        }
    }

    public void handleRuntimeException(SlingHttpServletRequest request, SlingHttpServletResponse response, RuntimeException ex) throws IOException {
        ResultJsonModel result = new ResultJsonModel(ERROR_STATUS_CODE, "Unexpected server error", ex.getMessage(), null);
        handle(request, response, result, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    }

    public void handleModelValidationError(SlingHttpServletRequest request, SlingHttpServletResponse response, Errors errors) throws IOException {
        ResultJsonModel result = new ResultJsonModel(VALIDATION_FAILURE_STATUS_CODE, "Validation failed", null, errors.asList());
        handle(request, response, result, HttpServletResponse.SC_BAD_REQUEST);
    }

    private void handle(SlingHttpServletRequest request, SlingHttpServletResponse response, ResultJsonModel result, int status)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
        result.setAuthContext(createAuthContext(request));
        JSON_WRITER.writeValue(response.getWriter(), result);
    }

    private AuthContext createAuthContext(SlingHttpServletRequest request) {
        boolean authenticated = request.getAuthType() != null;
        String userId = "anonymous";
        if (authenticated && request.getUserPrincipal() != null) {
            userId = request.getUserPrincipal().getName();
        }
        return new AuthContext(userId);
    }

    private void handle(RestActionResult.ResponseHandler responseHandler, SlingHttpServletResponse response) throws IOException {
        if (responseHandler != null) {
            responseHandler.handle(response);
        } else {
            LOG.error("No response handler");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    private static class ResultJsonModel {

        private final String status;
        private final String message;
        private final String messageDetails;
        private final Object entity;
        private AuthContext authContext;

        ResultJsonModel(RestActionResult restActionResult) {
            status = restActionResult.getStatus().toString();
            message = restActionResult.getMessage();
            messageDetails = restActionResult.getMessageDetails();
            entity = restActionResult.getEntity();
        }

        ResultJsonModel(String status, String message, String messageDetails, Object entity) {
            this.status = status;
            this.message = message;
            this.messageDetails = messageDetails;
            this.entity = entity;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public String getMessageDetails() {
            return messageDetails;
        }

        public Object getEntity() {
            return entity;
        }

        public AuthContext getAuthContext() {
            return authContext;
        }

        public void setAuthContext(AuthContext authContext) {
            this.authContext = authContext;
        }
    }

    private static class AuthContext {
        private final String userId;

        AuthContext(String userId) {
            this.userId = userId;
        }

        public String getUserId() {
            return userId;
        }
    }
}
