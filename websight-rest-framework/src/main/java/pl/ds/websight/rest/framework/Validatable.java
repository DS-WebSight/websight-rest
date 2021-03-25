package pl.ds.websight.rest.framework;

/**
 * Rest Actions models may optionally implements this interface to do post process
 * validation. Method is invoked after performing standard validation if no errors.
 */
public interface Validatable {

    Errors validate();
}
