package red.mvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.Utils.ModelAndView;
import red.mvc.exception.UrlMappingException;
import red.mvc.listener.RedContextListener;

public class FrontControllerServlet extends HttpServlet {
    Map<UrlMethod, Mapping> urlMapping = new HashMap<>();
    String prefix = null;
    String suffix = null;

    // @SuppressWarnings("unchecked")
    // private Map<UrlMethod, Mapping> getUrlMapping() {
        
    //     return (Map<UrlMethod, Mapping>) getServletContext().getAttribute(RedContextListener.ATTR_URL_MAPPING);
    // }

    public void init() throws ServletException {
        super.init();
        urlMapping = (Map<UrlMethod, Mapping>) getServletContext().getAttribute(RedContextListener.ATTR_URL_MAPPING);
        prefix = (String)getServletContext().getAttribute("prefixe");
        suffix = (String) getServletContext().getAttribute("suffixe");
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
            if (resultat instanceof ModelAndView) {
                ModelAndView mav = (ModelAndView) resultat;
                traiterModelAndView(mav, req, res);
            } else {
                out.print(resultat);
            }
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
        Mapping mapping = urlMapping.get(cle);
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

    public void traiterModelAndView(ModelAndView mav, HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {   
        
        if (mav != null) {
            if (mav.getAttribute() != null) {
                for (Map.Entry<String, Object> entry : mav.getAttribute().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
            }

            req.getRequestDispatcher(prefix + mav.getViewName() + suffix).forward(req, res);
        }
    }
}