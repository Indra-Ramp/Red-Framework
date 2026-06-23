package red.mvc.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import red.mvc.Utils.Utils;
import red.mvc.annotation.Controller;

public class FrontControllerServlet extends HttpServlet {

    private List<Class<?>> listController;

    @Override
    public void init() throws ServletException{
        String packageName = this.getInitParameter("controllersPackage");
        try {
            List<Class<?>> classes = Utils.scanPackage(packageName);
            listController = Utils.getAnnotedClasses(classes, Controller.class);

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
        String url = req.getRequestURL().toString();
        PrintWriter out = res.getWriter();
        for(Class c : listController){
            out.println(c.getName());
        }
        out.println(url);
        out.close();
    }
}