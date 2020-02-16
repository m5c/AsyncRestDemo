package eu.kartoffelquadrat.asyncrestdemo;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Web-UI Controller with a single page.
 */
@Controller
public class HtmlAccessController {

    /**
     * Calling the application root is answered with an HTML page, that contains the Javascript browser chat-client.
     * See resources/static folder for the client implementation.
     *
     * @return HTML page as string.
     */
    @RequestMapping(method = RequestMethod.GET, value = "/")
    public String index() {
        return "index.html";
    }

}
