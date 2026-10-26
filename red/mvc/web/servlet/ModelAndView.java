package red.mvc.web.servlet;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private Map<String, Object> attributes;
    private String viewName;

    public ModelAndView() {
        setAttributes(new HashMap<>());
    };

    public ModelAndView(String viewName) {
        setViewName(viewName);
        setAttributes(new HashMap<>());
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    private void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public void addObject(String name, Object value) {
        getAttributes().put(name, value);
    }

    // public Object getAttribute(String name) {
    //     return getAttributes().get(name);
    // }
}
