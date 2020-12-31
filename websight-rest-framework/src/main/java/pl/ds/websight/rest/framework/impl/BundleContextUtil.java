package pl.ds.websight.rest.framework.impl;

import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

final class BundleContextUtil {

    private static final String NO_FILTER = null;

    /**
     * Wrapper for {@link BundleContext#getAllServiceReferences(String, String)} method to handle bad OSGi API.
     * Call {@link BundleContext#getAllServiceReferences(String, String)} with given class name and no filter,
     * handles {@link InvalidSyntaxException} and null result if no services.
     */
    @SuppressWarnings("unchecked")
    @NotNull
    static <T> ServiceReference<T>[] getAllServiceReferences(BundleContext bundleContext, Class<T> clazz) {
        try {
            ServiceReference<T>[] allServiceReferences = (ServiceReference<T>[]) bundleContext
                    .getAllServiceReferences(clazz.getName(), NO_FILTER);
            return allServiceReferences != null ? allServiceReferences : new ServiceReference[0];
        } catch (InvalidSyntaxException e) {
            // Cannot happen because there is no filter.
            throw new IllegalStateException("Invalid filter for searching action services.", e);
        }
    }

    private BundleContextUtil() {
        // no instance
    }
}
