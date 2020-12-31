package pl.ds.websight.rest.framework.rest;

import org.osgi.service.component.annotations.Component;
import pl.ds.websight.rest.framework.RestAction;
import pl.ds.websight.rest.framework.RestActionResult;
import pl.ds.websight.rest.framework.annotations.SlingAction;

@Component (property = "sling.auth.requirements=-/apps/websight-rest-framework/bin/check-authentication")
@SlingAction(SlingAction.HttpMethod.GET)
public class CheckAuthenticationRestAction implements RestAction<Void, Void> {

    @Override
    public RestActionResult<Void> perform(Void noModel) {
        return RestActionResult.success();
    }
}
