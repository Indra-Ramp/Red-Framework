package red.mvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import red.mvc.annotation.Api;
import red.mvc.exception.UrlNotFoundException;
import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;
import red.mvc.Utils.Utils;
import red.mvc.web.servlet.ModelAndView;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Elia
 */
@MultipartConfig 
public class FrontControllerServlet extends HttpServlet {

    private Map<UrlMethod, Mapping> urlMappings;
    private String suffix;
    private String prefix;

    @Override
    @SuppressWarnings("unchecked")
    public void init() throws ServletException {
        urlMappings = (Map<UrlMethod, Mapping>) this.getServletContext().getAttribute("urlMappings");
        suffix = (String) this.getServletContext().getInitParameter("suffix");
        prefix = (String) this.getServletContext().getInitParameter("prefix");
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        processRequest(req, res);
    }

    /**
     * @author <b>Elia</b>
     * @param req : The object representation of the request
     * @param res : The object representation of the response
     * @throws ServletException
     * @throws IOException
     */

    public void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if(urlMappings == null) {
            throw new ServletException("NULL");
        }
        String uri = req.getPathInfo();
        if(uri != null && uri.startsWith("/WEB-INF")) {
            req.getServletContext().getNamedDispatcher("jsp").forward(req, res);
            return;
        }
        UrlMethod method = new UrlMethod();
        method.setUrl(uri);
        method.setMethode(req.getMethod());
        Mapping mapping = urlMappings.get(method);
        if(mapping == null) {
            UrlNotFoundException ex = new UrlNotFoundException(urlMappings, method);
            throw new ServletException(ex);
        }
        
        try {
            Object result = Utils.invokeMapping(mapping,this.getServletContext(), req);
            if(result instanceof ModelAndView) {
                ModelAndView castedResult = (ModelAndView) result;
                String viewName = prefix + castedResult.getViewName() + suffix;
                // out.println(viewName);
                RequestDispatcher dis = req.getRequestDispatcher(viewName);
                for(Map.Entry<String, Object> entry : castedResult.getAttributes().entrySet()) {
                    req.setAttribute(entry.getKey(), entry.getValue());
                }
                // out.println(dis);
                dis.forward(req, res);
                return;
            }
            try(PrintWriter out = res.getWriter();) {
                if(mapping.getMethod().isAnnotationPresent(Api.class)) {
                    res.addHeader("Content-Type", "application/json");
                    if(result != null && result.getClass().equals(String.class)) {
                        out.println(result);
                        return;
                    }
                    out.println(Utils.toJson(result));
                    return;
                }
                out.println(result);
            }
        } catch(Exception e) {
            throw new ServletException(e);
        }
    }
}
