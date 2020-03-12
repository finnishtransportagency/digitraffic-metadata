package fi.livi.digitraffic.tie.controller.handler;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;


@RestController
@RequestMapping("/error")
@ConditionalOnWebApplication
public class SimpleErrorController implements ErrorController {
    private final ErrorAttributes errorAttributes;

    private static final boolean INCLUDE_STACK_TRACE = false;

    @Autowired
    public SimpleErrorController(final ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public Map<String, Object> error(final HttpServletRequest aRequest) {
        return getErrorAttributes(aRequest);
    }

    private Map<String, Object> getErrorAttributes(final HttpServletRequest request) {
        final WebRequest webRequest = new ServletWebRequest(request);

        return errorAttributes.getErrorAttributes(webRequest, INCLUDE_STACK_TRACE);
    }
}
