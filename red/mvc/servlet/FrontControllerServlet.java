package red.mvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.Utils;
import red.mvc.annotation.Controller;
import red.mvc.exception.UrlMappingException;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> listController;
    private Map<String, Mapping> urlMapping;

    @Override
    public void init() throws ServletException{
        String packageName = this.getInitParameter("controllersPackage");
        try {
            List<Class<?>> classes = Utils.scanPackage(packageName);
            listController = Utils.getAnnotedClasses(classes, Controller.class);
            urlMapping = Utils.getUrlMapping(listController);
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        String url = req.getRequestURI().substring(req.getContextPath().length());
        PrintWriter out = res.getWriter();

        try {
            Mapping mapping = trouverMapping(url);
            out.println("Url demandee : " + url);
            out.println("Controller   : " + mapping.getController().getName());
            out.println("Methode      : " + mapping.getMethode().getName());
        } catch (UrlMappingException e) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("Erreur : " + e.getMessage());
        }

        out.close();
    }

    // Bonus : leve une exception si l'url demandee n'existe pas dans le mapping
    private Mapping trouverMapping(String url) throws UrlMappingException {
        Mapping mapping = urlMapping.get(url);
        if (mapping == null) {
            throw new UrlMappingException("Aucune methode ne correspond a l'url : " + url);
        }
        return mapping;
    }
}