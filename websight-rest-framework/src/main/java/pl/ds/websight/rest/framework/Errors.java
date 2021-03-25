package pl.ds.websight.rest.framework;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class Errors {

    private final List<Error> errorsList = new ArrayList<>();

    private Errors() {
        // no direct instances
    }

    public static Errors createErrors() {
        return new Errors();
    }

    public static Errors of(String path, Object invalidValue, String message) {
        return new Errors().add(path, invalidValue, message);
    }

    public static Errors of(Error... errors) {
        return new Errors().add(errors);
    }

    public Errors add(String path, Object invalidValue, String message) {
        errorsList.add(new Error(path, invalidValue, message));
        return this;
    }

    /**
     * Add all errors. Skips null objects;
     * @param errors to add to list
     * @return self
     */
    public Errors add(Error... errors) {
        if (errors != null) {
            errorsList.addAll(Arrays.stream(errors).filter(Objects::nonNull).collect(Collectors.toList()));
        }
        return this;
    }

    public List<Error> asList() {
        return Collections.unmodifiableList(errorsList);
    }

    public boolean isEmpty() {
        return errorsList.isEmpty();
    }

    public static class Error {

        private final String path;
        private final Object invalidValue;
        private final String message;

        private Error(String path, Object invalidValue, String message) {
            this.path = path;
            this.invalidValue = invalidValue;
            this.message = message;
        }

        public static Error of(String path, Object invalidValue, String message) {
            return new Error(path, invalidValue, message);
        }

        public String getPath() {
            return path;
        }

        public Object getInvalidValue() {
            return invalidValue;
        }

        public String getMessage() {
            return message;
        }
    }
}
