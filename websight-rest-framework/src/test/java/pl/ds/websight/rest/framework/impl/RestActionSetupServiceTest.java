package pl.ds.websight.rest.framework.impl;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.ServletResolverConstants;
import org.apache.sling.models.annotations.Model;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.Bundle;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.PrimaryTypes;
import pl.ds.websight.rest.framework.annotations.ResourceTypes;
import pl.ds.websight.rest.framework.annotations.SlingAction;

import java.util.Dictionary;

import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_EXTENSIONS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_METHODS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_PATHS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_RESOURCE_TYPES;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RestActionSetupServiceTest {

    @Mock
    private FrameworkUtilService frameworkUtilService;

    @InjectMocks
    private RestActionSetupService cut;

    @Test
    void shouldSetUrlPropertiesForPathAction() {
        mockBundleFor(HelloPathRestAction.class);
        RestAction<?, ?> restAction = new HelloPathRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertEquals("/apps/websight-rest-framework/bin/hello-path", properties.get(SLING_SERVLET_PATHS));
        assertEquals("action", properties.get(SLING_SERVLET_EXTENSIONS));
        assertEquals(true, properties.get("sling.servlet.paths.strict"));
    }

    @Test
    void shouldSetUrlPropertiesForPrimaryTypeAction() {
        mockBundleFor(HelloPrimaryTypeRestAction.class);
        RestAction<?, ?> restAction = new HelloPrimaryTypeRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertEquals(ServletResolverConstants.DEFAULT_RESOURCE_TYPE, properties.get(SLING_SERVLET_RESOURCE_TYPES));
        assertEquals("websight-rest-framework.hello-primary-type", properties.get(SLING_SERVLET_SELECTORS));
        assertEquals("action", properties.get(SLING_SERVLET_EXTENSIONS));
    }

    @Test
    void shouldSetUrlPropertiesForResourceTypeAction() {
        mockBundleFor(HelloResourceTypePostRestAction.class);
        RestAction<?, ?> restAction = new HelloResourceTypePostRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertArrayEquals(new String[] { "some/example/resource/type" }, ((String[]) properties.get(SLING_SERVLET_RESOURCE_TYPES)));
        assertEquals("websight-rest-framework.hello-resource-type-post", properties.get(SLING_SERVLET_SELECTORS));
        assertEquals("action", properties.get(SLING_SERVLET_EXTENSIONS));
    }

    @Test
    void shouldSetGetMethod() {
        mockBundleFor(HelloPathRestAction.class);
        RestAction<?, ?> restAction = new HelloPathRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertEquals("GET", properties.get(SLING_SERVLET_METHODS));
    }

    @Test
    void shouldSetPostMethodByDefault() {
        mockBundleFor(HelloResourceTypePostRestAction.class);
        RestAction<?, ?> restAction = new HelloResourceTypePostRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertEquals("POST", properties.get(SLING_SERVLET_METHODS));
    }

    @Test
    void shouldSetActionClass() {
        mockBundleFor(HelloResourceTypePostRestAction.class);
        RestAction<?, ?> restAction = new HelloResourceTypePostRestAction();

        Dictionary<String, Object> properties = cut.buildActionServletProperties(restAction);

        assertEquals(HelloResourceTypePostRestAction.class.getName(), properties.get("websight.action.class"));
    }

    @Test
    void shouldFailIfNoSlingActionAnnotation() {
        RestAction<?, ?> restAction = new InvalidNoSlingActionAnnotationRestAction();

        assertThrows(IllegalStateException.class,
                () -> cut.buildActionServletProperties(restAction),
                "No SlingAction annotation at class " + InvalidNoSlingActionAnnotationRestAction.class.getName());
    }

    @Test
    void shouldFailIfMultipleAnnotations() {
        RestAction<?, ?> restAction = new InvalidMultipleAnnotationsRestAction();

        assertThrows(IllegalStateException.class,
                () -> cut.buildActionServletProperties(restAction),
                "RestAction cannot be annotated with both PrimaryTypes" +
                        "and ResourceTypes annotations" + InvalidMultipleAnnotationsRestAction.class.getName());
    }

    @Test
    void shouldReturnRestActionModelType() {
        RestAction<?, ?> restAction = new HelloPathRestAction();

        Class<?> modelClass = cut.getModelClass(restAction);

        assertEquals(HelloPathModel.class, modelClass);
    }

    @Test
    void shouldAllowVoidAsRestActionModelType() {
        RestAction<?, ?> restAction = new ValidRestActionWithVoidModel();

        Class<?> modelClass = cut.getModelClass(restAction);

        assertEquals(Void.class, modelClass);
    }

    @Test
    void shouldFailIfNotSlingModel() {
        RestAction<?, ?> restAction = new InvalidNonSlingModelRestAction();

        assertThrows(
                IllegalStateException.class,
                () -> cut.getModelClass(restAction),
                "Model type of action class " +
                        InvalidNonSlingModelRestAction.class.getName() +
                        "is not Sling model adaptable from request.");
    }

    @Test
    void shouldFailIfWrongAdaptableInModel() {
        RestAction<?, ?> restAction = new InvalidResourceRestAction();

        assertThrows(
                IllegalStateException.class,
                () -> cut.getModelClass(restAction),
                "Model type of action class " +
                        InvalidResourceRestAction.class.getName() +
                        "is not Sling model adaptable from request.");
    }

    @Test
    void shouldReturnRestActionModelWhenClassHierarchyIsComplex() {
        RestAction<?, ?> restAction = new ComplexInheritanceRestAction();

        Class<?> modelClass = cut.getModelClass(restAction);

        assertEquals(ValidModel.class, modelClass);
    }

    private void mockBundleFor(Class<?> clazz) {
        Bundle bundle = mock(Bundle.class);
        when(bundle.getSymbolicName()).thenReturn("websight-rest-framework");
        when(frameworkUtilService.getBundle(clazz)).thenReturn(bundle);
    }

    @Model(adaptables = SlingHttpServletRequest.class)
    static class ValidModel {
    }

    static class ValidRestActionWithVoidModel implements RestAction<Void, Void> {
        @Override
        public RestActionResult<Void> perform(Void model) {
            return null;
        }
    }

    static class InvalidNoSlingActionAnnotationRestAction implements RestAction<ValidModel, Void> {
        @Override
        public RestActionResult<Void> perform(ValidModel model) {
            return null;
        }
    }

    @Model(adaptables = Resource.class)
    static class InvalidResourceModel {
    }

    static class InvalidResourceRestAction implements RestAction<InvalidResourceModel, Void> {
        @Override
        public RestActionResult<Void> perform(InvalidResourceModel model) {
            return null;
        }
    }

    static class InvalidNonSlingModel {
    }

    static class InvalidNonSlingModelRestAction implements RestAction<InvalidNonSlingModel, Void> {
        @Override
        public RestActionResult<Void> perform(InvalidNonSlingModel model) {
            return null;
        }
    }

    @SlingAction
    @PrimaryTypes("some/primaryType")
    @ResourceTypes("some/resourceType")
    static class InvalidMultipleAnnotationsRestAction implements RestAction<RestActionSetupServiceTest.ValidModel, Void> {
        @Override
        public RestActionResult<Void> perform(RestActionSetupServiceTest.ValidModel model) {
            return null;
        }
    }

    static class SimpleDto {
    }

    static abstract class ComplexInheritanceRestActionBase<T> implements RestAction<ValidModel, T> {
    }

    static abstract class ComplexInheritanceRestActionSubclass extends ComplexInheritanceRestActionBase<SimpleDto> {
    }

    static class ComplexInheritanceRestAction extends ComplexInheritanceRestActionSubclass {
        @Override
        public RestActionResult<SimpleDto> perform(ValidModel model) {
            return null;
        }
    }
}
