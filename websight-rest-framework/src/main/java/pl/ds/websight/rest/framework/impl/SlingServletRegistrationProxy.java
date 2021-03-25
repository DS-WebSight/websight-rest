package pl.ds.websight.rest.framework.impl;

import org.apache.sling.models.factory.ModelFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.ds.websight.rest.framework.RestAction;

import javax.servlet.Servlet;
import javax.validation.Validator;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static pl.ds.websight.rest.framework.impl.BundleContextUtil.getAllServiceReferences;

@Component(immediate = true)
public class SlingServletRegistrationProxy {

    private static final Logger LOG = LoggerFactory.getLogger(SlingServletRegistrationProxy.class);

    @Reference
    private RestActionSetupService restActionSetupService;

    @Reference
    private ModelFactory modelFactory;

    @Reference
    private Validator validator;

    @Reference
    private RestResponseHandler restResponseHandler;

    private BundleContext bundleContext;
    private final Map<Long, ServiceRegistration<Servlet>> actionServletRegistrationByActionServiceId = new HashMap<>();

    @Activate
    @SuppressWarnings("unused")
    private synchronized void activate(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        ServiceReference<?>[] actionsReferences = getAllServiceReferences(bundleContext, RestAction.class);
        for (ServiceReference<?> actionReference : actionsReferences) {
            try {
                RestAction<?, ?> restAction = (RestAction<?, ?>) bundleContext.getService(actionReference);
                registerActionServlet(restAction, (Long) actionReference.getProperty(Constants.SERVICE_ID));
            } catch (RuntimeException e) {
                LOG.error("Cannot register rest action servlet.", e);
            }
        }
    }

    @Deactivate
    @SuppressWarnings("unused")
    private synchronized void deactivate() {
        this.bundleContext = null;
        // copy keys to avoid concurrent map modification
        Set<Long> actionServiceIds = new HashSet<>(actionServletRegistrationByActionServiceId.keySet());
        for (Long id : actionServiceIds) {
            unregisterActionServlet(id);
        }
    }

    @Reference(
            service = RestAction.class,
            cardinality = ReferenceCardinality.MULTIPLE,
            policy = ReferencePolicy.DYNAMIC)
    @SuppressWarnings("unused")
    private synchronized void bindRestAction(RestAction<?, ?> restAction, Map<String, ?> properties) {
        if (bundleContext != null) {
            registerActionServlet(restAction, (Long) properties.get(Constants.SERVICE_ID));
        } else {
            LOG.info("Delegating action {} registration to activate method. Bundle context not ready yet.", restAction.getClass());
        }
    }

    @SuppressWarnings("unused")
    private synchronized void unbindRestAction(RestAction<?, ?> restAction, Map<String, ?> properties) {
        unregisterActionServlet((Long) properties.get(Constants.SERVICE_ID));
    }

    private void registerActionServlet(RestAction<?, ?> restAction, Long restActionServiceId) {
        if (actionServletRegistrationByActionServiceId.containsKey(restActionServiceId)) {
            throw new IllegalStateException("Service already registered for rest action service id " + restActionServiceId);
        }
        Dictionary<String, Object> servletProperties = restActionSetupService.buildActionServletProperties(restAction);
        Class<?> restActionModelType = restActionSetupService.getModelClass(restAction);
        RestActionsServlet servlet = new RestActionsServlet(restAction, restActionModelType, modelFactory, validator, restResponseHandler);
        ServiceRegistration<Servlet> service = bundleContext.registerService(Servlet.class, servlet, servletProperties);
        actionServletRegistrationByActionServiceId.put(restActionServiceId, service);
        LOG.info("Rest action servlet for action class {} and rest action service id {} registered.",
                restAction.getClass(), restActionServiceId);
    }

    private void unregisterActionServlet(Long id) {
        ServiceRegistration<?> service = actionServletRegistrationByActionServiceId.get(id);
        if (service != null) {
            try {
                service.unregister();
                LOG.info("Servlet service for rest action with id {} unregistered.", id);
            } catch (IllegalStateException e) {
                LOG.warn("Servlet service for rest action with id {} already unregistered.", id);
            }
            actionServletRegistrationByActionServiceId.remove(id);
        } else {
            LOG.warn("Cannot find servlet service for rest action with id {} for unregister.", id);
        }
    }
}
