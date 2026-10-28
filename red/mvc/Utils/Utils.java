package red.mvc.Utils;

import java.io.File;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;

import org.springframework.context.ApplicationContext;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectWriter;

import red.mvc.helper.RedSessionHelper;
import red.mvc.annotation.UrlMapping;
import red.mvc.exception.DuplicateUrlException;
import red.mvc.web.servlet.RedSession;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

public class Utils {
    public static List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> ret = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String formatedName = packageName.replaceAll("\\.", "/");
        URL ressource = loader.getResource(formatedName);
        if(ressource == null) {
            throw new Exception("Package not found: " + packageName);
        }
        String path = ressource.getPath().replaceAll("%20", " ");
        // throw new Exception(path);
        File dir = new File(path);
        if(dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles();
            for(File f : files) {
                String fileName = f.getName();
                if(fileName.endsWith(".class")) {
                    String className = fileName.split("\\.")[0];
                    String fullClassName = packageName + "." + className;
                    Class<?> clazz = Class.forName(fullClassName);
                    ret.add(clazz);
                }
            }
        }
        return ret;
    }

    public static void getUrlMapping(List<Class<?>> classes, Class<? extends Annotation> annotation, Map<UrlMethod, Mapping> urlMappings) throws Exception {
        for(Class<?> clazz : classes) {
            if(clazz.isAnnotationPresent(annotation)) {
                List<Method> annotatedMethod = getAnnotatedMethod(clazz, UrlMapping.class);
                for(Method m : annotatedMethod) {
                    UrlMapping urlAnnotation = m.getAnnotation(UrlMapping.class);
                    String url = urlAnnotation.url();
                    Mapping map = new Mapping(clazz, m);
                    UrlMethod method = new UrlMethod();
                    method.setUrl(url);
                    method.setMethode(urlAnnotation.method());
                    if(urlMappings.containsKey(method)) {
                        DuplicateUrlException ex = new DuplicateUrlException();
                        ex.setExisting(urlMappings.get(method));
                        ex.setIntended(map);
                        ex.setUrl(method);
                        throw ex;
                    }
                    urlMappings.put(method, map);
                }
                // throw new Exception("PRESENT");
            }
        }
    }

    public static List<Method> getAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Method> ret = new ArrayList<>();
        Method[] methods = clazz.getDeclaredMethods();
        for(Method m : methods) {
            if(m.isAnnotationPresent(annotation)) {
                ret.add(m);
            }
        }
        return ret;
    }    

    public static Map<UrlMethod, Mapping> getUrlMapping(List<Class<?>> classes) throws DuplicateUrlException {
        Map<UrlMethod, Mapping> ret = new HashMap<>();
        for(Class<?> clazz : classes) {
            List<Method> annotatedMethod = Utils.getAnnotatedMethod(clazz, UrlMapping.class);
            for(Method m : annotatedMethod) {
                UrlMapping annotation = m.getAnnotation(UrlMapping.class);
                String url = annotation.url();
                Mapping map = new Mapping(clazz, m);
                UrlMethod method = new UrlMethod();
                method.setUrl(url);
                method.setMethode(annotation.method());
                if(ret.containsKey(method)) {
                    DuplicateUrlException ex = new DuplicateUrlException();
                    ex.setExisting(ret.get(method));
                    ex.setIntended(map);
                    ex.setUrl(method);
                    throw ex;
                }
                ret.put(method, map);
            }
        }
        return ret;
    }

    private static int findContextIndex(Class<?>[] parameterTypes) {
        if(parameterTypes == null) {
            return -1;
        }
        return IntStream.range(0, parameterTypes.length)
        .filter(i -> parameterTypes[i] == ApplicationContext.class)
        .findFirst()
        .orElse(-1);
    }

    private static void fillParameters(Parameter[] parameters, HttpServletRequest req, Object[] values) throws Exception {
        Map<String, Object> assoc = new HashMap<>();
        Enumeration<String> params = req.getParameterNames();
        String param = null;
        while(params.hasMoreElements()) {
            param = params.nextElement();
            assoc.put(param, req.getParameterValues(param));
            // param = params.nextElement();
        }
        int i = 0;
        for(Parameter p : parameters) {
            values[i] = TypeConverter.convert(assoc.get(p.getName()), p.getType(), p.getParameterizedType());
            i++;
        }
    }

    private static int findSpecificArgIndex(Class<?>[] parameterTypes, Class<?> clazz) {
        if (parameterTypes == null) {
            return -1;
        }
        return IntStream.range(0, parameterTypes.length)
                .filter(i -> parameterTypes[i].equals(clazz))
                .findFirst()
                .orElse(-1);
    }

    public static Object invokeMapping(Mapping mapping, ServletContext context, HttpServletRequest req) throws Exception {
        HttpSession httpSession = null;
        httpSession = req.getSession(true);
        Method method = mapping.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int contextIndex = findSpecificArgIndex(parameterTypes, ApplicationContext.class);
        int sessionIndex = findSpecificArgIndex(parameterTypes, RedSession.class);
        Object[] parameters = new Object[parameterTypes.length];
        fillParametersObject(method.getParameters(), req, parameters);
        if(contextIndex != -1) {
            parameters[contextIndex] = context.getAttribute("springContainer");
        }
        RedSession session = null;
        if(sessionIndex != -1){
            Map<String, RedSession> sessions = (Map<String, RedSession>) context.getAttribute("sessions");
            session = sessions.get(httpSession.getId());
            if(session == null){
                session = new RedSession();
                sessions.put(httpSession.getId(), session);
            }
            RedSessionHelper.fillWithSession(session,httpSession);
            parameters[sessionIndex] = session;
        }
        Object invoking = mapping.getController().getConstructor().newInstance();
        Object ret = mapping.getMethod().invoke(invoking, parameters);
        if(session != null){
            RedSessionHelper.overrideHttpSession(session, httpSession);
        }

        return ret;
    }

    public static String toJson(Object object) throws JsonProcessingException {
        ObjectWriter ow = JsonResources.getObjectWriter();
        return ow.writeValueAsString(object);
    }

    private static void fillParametersObject(Parameter[] parameters, HttpServletRequest req, Object[] values)
            throws Exception {
        Enumeration<String> params = req.getParameterNames();

        String contentType = req.getContentType();
        Map<String, String[]> temp = new HashMap<>();
        // Map<String, String[]> temp2 = new HashMap<>();

        boolean isMultipart = contentType != null && contentType.toLowerCase().startsWith("multipart/");
        if (isMultipart) {
            List<String> paramsList = Collections.list(params);
            Set<String> inputs = new HashSet<>();
            Collection<Part> parts = req.getParts();
            List<String> paramList2 = Collections.list(params);
            Map<String, Integer> indexes = new HashMap<>();
            Map<String, Boolean> multiples = new HashMap<>();
            for (Part p : parts) {
                paramsList.add(p.getName());
                if (p.getSubmittedFileName() != null && !p.getSubmittedFileName().isEmpty()) {
                    String inputName = p.getName();
                    if (!inputs.contains(inputName)) {
                        for (Parameter para : parameters) {
                            if (para.getName().equals(inputName)) {
                                inputs.add(inputName);
                                if (List.class.isAssignableFrom(para.getType())) {
                                    multiples.put(inputName, true);
                                }
                                break;
                            }
                        }
                    }
                    boolean multiple = multiples.getOrDefault(inputName, false);
                    int index = indexes.getOrDefault(inputName, 0);
                    indexes.put(inputName, index + 1);
                    String fileNameFull = p.getSubmittedFileName();
                    String[] fileParts = fileNameFull.split("\\.");
                    String fileName = fileParts[0];
                    String ext = "";
                    if (fileParts.length > 1) {
                        ext = fileParts[1];
                    }
                    String iName = inputName + ".fileName";
                    String iExt = inputName + ".ext";
                    String iBytes = inputName + ".bytes";
                    String iName2 = inputName + "[" + index + "]" + ".fileName";
                    String iExt2 = inputName + "[" + index + "]" + ".ext";
                    String iBytes2 = inputName + "[" + index + "]" + ".bytes";
                    if (!multiple) {
                        paramsList.addAll(List.of(iName, iExt, iBytes));
                    } else {
                        paramsList.addAll(List.of(iName2, iExt2, iBytes2));
                    }
                    temp.put(iName, new String[] { fileName });
                    temp.put(iExt, new String[] { ext });
                    temp.put(iName2, new String[] { fileName });
                    temp.put(iExt2, new String[] { ext });
                    byte[] bytes = null;
                    try (InputStream stream = p.getInputStream()) {
                        bytes = stream.readAllBytes();
                    }
                    String b64 = Base64.getEncoder().encodeToString(bytes);
                    temp.put(iBytes, new String[] { b64 });
                    temp.put(iBytes2, new String[] { b64 });
                    // index++;
                }
            }

            params = Collections.enumeration(paramsList);
            // params = Collections.enumeration(paramList2);
        }

        System.out.println("PARAM: " + params.hasMoreElements());
        BindingContext ctx = new BindingContext();
        for (Parameter p : parameters) {
            ctx.registerRootType(p.getName(), p.getType(), p.getParameterizedType());
        }
        while (params.hasMoreElements()) {
            String param = params.nextElement();
            if (req.getParameterValues(param) != null) {
                ctx.insertFormValue(param, req.getParameterValues(param));
            } else if (temp.get(param) != null) {
                ctx.insertFormValue(param, temp.get(param));
            }
        }
        // System.out.println(ctx.getRoots());
        Map<String, Object> result = ctx.instantiateAll();
        for (int i = 0; i < parameters.length; i++) {
            values[i] = result.getOrDefault(parameters[i].getName(), null);
            // System.out.println("VALUES: " + values[i]);
        }
    }

    
}