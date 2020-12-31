package pl.ds.websight.rest.framework;

import org.apache.sling.api.SlingHttpServletResponse;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class RestActionResult<R> {

    public enum Status {
        SUCCESS, FAILURE, FREE_FORM_RESPONSE
    }

    private final Status status;
    private final String message;
    private final String messageDetails;
    private final R entity;
    private final ResponseHandler responseHandler;

    /**
     * Creates result with status SUCCESS.
     */
    public static <R> RestActionResult<R> success() {
        return new RestActionResult<>(Status.SUCCESS, null, null, null, null);
    }

    /**
     * Creates result with status SUCCESS.
     *
     * @param message        info message (might be used as notification for end user)
     * @param messageDetails info message details (might be used as notification for end user)
     */
    public static <R> RestActionResult<R> success(String message, String messageDetails) {
        return new RestActionResult<>(Status.SUCCESS, message, messageDetails, null, null);
    }

    /**
     * Creates result with status SUCCESS.
     *
     * @param entity object mapped to json and used as value for entity key in response
     */
    public static <R> RestActionResult<R> success(R entity) {
        return new RestActionResult<>(Status.SUCCESS, null, null, entity, null);
    }

    /**
     * Creates result with status SUCCESS.
     *
     * @param message        info message (might be used as notification for end user)
     * @param messageDetails info message details (might be used as notification for end user)
     * @param entity         object mapped to json and used as value for entity key in response
     */
    public static <R> RestActionResult<R> success(String message, String messageDetails, R entity) {
        return new RestActionResult<>(Status.SUCCESS, message, messageDetails, entity, null);
    }

    /**
     * Creates result with status FAILURE.
     */
    public static <R> RestActionResult<R> failure() {
        return new RestActionResult<>(Status.FAILURE, null, null, null, null);
    }

    /**
     * Creates result with status FAILURE.
     *
     * @param message        info message (might be used as notification for end user)
     * @param messageDetails info message details (might be used as notification for end user)
     */
    public static <R> RestActionResult<R> failure(String message, String messageDetails) {
        return new RestActionResult<>(Status.FAILURE, message, messageDetails, null, null);
    }

    /**
     * Creates result with status FAILURE.
     *
     * @param entity object mapped to json and used as value for entity key in response
     */
    public static <R> RestActionResult<R> failure(R entity) {
        return new RestActionResult<>(Status.FAILURE, null, null, entity, null);
    }

    /**
     * Creates result with status FAILURE.
     *
     * @param message        info message (might be used as notification for end user)
     * @param messageDetails info message details (might be used as notification for end user)
     * @param entity         object mapped to json and used as value for entity key in response
     */
    public static <R> RestActionResult<R> failure(String message, String messageDetails, R entity) {
        return new RestActionResult<>(Status.FAILURE, message, messageDetails, entity, null);
    }

    /**
     * Creates result with status FREE_FORM_RESPONSE.
     *
     * @param responseHandler response handler to handle the response
     */
    public static <R> RestActionResult<R> freeFormResponse(@NotNull RestActionResult.ResponseHandler responseHandler) {
        return new RestActionResult<>(Status.FREE_FORM_RESPONSE, null, null, null, responseHandler);
    }

    private RestActionResult(Status status, String message, String messageDetails, R entity, ResponseHandler responseHandler) {
        this.status = status;
        this.message = message;
        this.messageDetails = messageDetails;
        this.entity = entity;
        this.responseHandler = responseHandler;
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageDetails() {
        return messageDetails;
    }

    public R getEntity() {
        return entity;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    /**
     * Implement to provide direct response handler from Rest Action via Rest Action Result.
     */
    public interface ResponseHandler {

        void handle(SlingHttpServletResponse response) throws IOException;
    }
}
