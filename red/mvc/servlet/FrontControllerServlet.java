package red.mvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.exception.UrlMappingException;
import red.mvc.listener.RedContextListener;

public class FrontControllerServlet extends HttpServlet {

    @SuppressWarnings("unchecked")
    private Map<UrlMethod, Mapping> getUrlMapping() {
        return (Map<UrlMethod, Mapping>) getServletContext().getAttribute(RedContextListener.ATTR_URL_MAPPING);
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
        String httpMethod = req.getMethod();
        PrintWriter out = res.getWriter();

        try {
            Mapping mapping = trouverMapping(url, httpMethod);
            Object resultat = invoquer(mapping);
            out.print(resultat);
        } catch (UrlMappingException e) {
            res.setStatus(HttpServletResponse.SC_NOT_FOUND);
            out.println("Erreur : " + e.getMessage());
        } catch (ReflectiveOperationException e) {
            res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.println("Erreur lors de l'invocation : " + e.getMessage());
        }

        out.close();
    }

    private Mapping trouverMapping(String url, String httpMethod) throws UrlMappingException {
        UrlMethod cle = new UrlMethod(url, httpMethod);
        Mapping mapping = getUrlMapping().get(cle);
        if (mapping == null) {
            throw new UrlMappingException("Aucune methode ne correspond a : " + httpMethod + " " + url);
        }
        return mapping;
    }

    private Object invoquer(Mapping mapping) throws ReflectiveOperationException {
        Object instance = mapping.getController().getDeclaredConstructor().newInstance();
        try {
            return mapping.getMethode().invoke(instance);
        } catch (InvocationTargetException e) {
            throw new ReflectiveOperationException(e.getCause());
        }
    }
}