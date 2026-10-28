package red.mvc.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import red.mvc.web.servlet.RedSession;
import red.mvc.annotation.Controller;
import red.mvc.config.AppProperties;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.Utils.Utils;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class RedContextListener implements ServletContextListener {

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // TODO Auto-generated method stub
        ServletContextListener.super.contextDestroyed(sce);
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // TODO Auto-generated method stub
        ServletContextListener.super.contextInitialized(sce);
        ServletContext context = sce.getServletContext();
        String packageName = context.getInitParameter("controllersPackage");
        // System.out.println("--------------------------------");
        // System.out.println(packageName);
        // System.out.println("--------------------------------");
        try {
            try {
                AppProperties.loadProperties();
                List<Class<?>> classes = Utils.scanPackage(packageName);
                Map<UrlMethod, Mapping> urlMapping = new HashMap<>();
                Utils.getUrlMapping(classes, Controller.class, urlMapping);
                context.setAttribute("urlMappings", urlMapping);
                ApplicationContext ctx = new ClassPathXmlApplicationContext("applicationContext.xml");
                context.setAttribute("springContainer", ctx);
                Map<String, RedSession> sessions = new HashMap<>();
                context.setAttribute("sessions", sessions); 
            } catch (Exception e) {
                throw new ServletException(e);
            }
        } catch (Exception e) {
            System.out.println("--------------------------------");
            System.out.println(e.getMessage());
            System.out.println("---------------------------------");
            throw new RuntimeException(e);
        }
    }

}
