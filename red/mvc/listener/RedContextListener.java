package red.mvc.listener;

import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.Utils.Utils;
import red.mvc.annotation.Controller;
import jakarta.servlet.annotation.WebListener;
@WebListener
public class RedContextListener implements ServletContextListener {

    public static final String ATTR_URL_MAPPING = "red.urlMapping";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String packageName = context.getInitParameter("controllersPackage");

        try {
            List<Class<?>> classes = Utils.scanPackage(packageName);
            List<Class<?>> controllers = Utils.getAnnotedClasses(classes, Controller.class);
            Map<UrlMethod, Mapping> urlMapping = Utils.getUrlMapping(controllers);

            context.setAttribute(ATTR_URL_MAPPING, urlMapping);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du Red-Framework", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(ATTR_URL_MAPPING);
    }
}