package red.mvc.Utils;

import java.util.HashMap;

public class ModelAndView {
    private String viewName;
    private HashMap<String, Object> attribute;

    public ModelAndView() {
    }



    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    

    public ModelAndView(String viewName, HashMap<String, Object> attribute) {
        this.viewName = viewName;
        this.attribute = attribute;
    }
    
    public void addAttribute(String key, Object value) {
        if (attribute == null) {
            attribute = new HashMap<>();
        }
        attribute.put(key, value);
    }
    public String getViewName() {
        return viewName;
    }
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }
    public HashMap<String, Object> getAttribute() {
        return attribute;
    }
    public void setAttribute(HashMap<String, Object> attribute) {
        this.attribute = attribute;
    }

    
    
}
