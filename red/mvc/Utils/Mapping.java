package red.mvc.Utils;

import java.lang.reflect.Method;

public class Mapping {
    private Class<?> controller;
    private Method methode;

    public Mapping() {
    }

    public Mapping(Class<?> controller, Method methode) {
        this.controller = controller;
        this.methode = methode;
    }
    
    public Class<?> getController() {
        return controller;
    }
    public void setController(Class<?> controller) {
        this.controller = controller;
    }
    public Method getMethode() {
        return methode;
    }
    public void setMethode(Method methode) {
        this.methode = methode;
    }
}
