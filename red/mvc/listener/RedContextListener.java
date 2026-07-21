package red.mvc.listener;

import java.util.List;
import java.util.Map;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.Utils.Utils;
import red.mvc.annotation.Controller;
import jakarta.servlet.annotation.WebListener;
import org.springframework.context.ApplicationContext;
@WebListener
public class RedContextListener implements ServletContextListener {

    public static final String ATTR_URL_MAPPING = "red.urlMapping";

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext context = sce.getServletContext();
        String packageName = context.getInitParameter("controllersPackage");
        String prefix = context.getInitParameter("prefixe");
        String suffix = context.getInitParameter("suffixe");
        ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
        context.setAttribute("applicationContext", ctx);
        System.out.println(prefix);
        System.out.println(suffix);

        try {
            List<Class<?>> classes = Utils.scanPackage(packageName);
            List<Class<?>> controllers = Utils.getAnnotedClasses(classes, Controller.class);
            Map<UrlMethod, Mapping> urlMapping = Utils.getUrlMapping(controllers);

            context.setAttribute(ATTR_URL_MAPPING, urlMapping);
            context.setAttribute("prefixe", prefix);
            context.setAttribute("suffixe", suffix);
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'initialisation du Red-Framework", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        sce.getServletContext().removeAttribute(ATTR_URL_MAPPING);
    }


}