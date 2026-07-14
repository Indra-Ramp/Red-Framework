package red.mvc.Utils;

import java.io.File;
import java.net.URL;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.mvc.annotation.UrlMapping;
import red.mvc.exception.UrlMappingException;

public class Utils {
    public static List<Class<?>> scanPackage(String namePackage) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String nameReplaced = namePackage.replaceAll("\\.", "/");
        URL resource = loader.getResource(nameReplaced);
        if(resource == null){
            throw new Exception("Package not found: " + namePackage);
        }

        String path = resource.getPath().replaceAll("%20", " ");
        File file = new File(path);
        if(file.exists() && file.isDirectory()){
            File[] files = file.listFiles();
            for(File f : files){
                String fileName = f.getName();
                if(fileName.endsWith(".class")){
                    String className = fileName.split("\\.")[0];
                    String NameEntier = namePackage+"."+className;
                    Class<?> clazz = Class.forName(NameEntier);
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }

    public static List<Class<?>> getAnnotedClasses(List<Class<?>> classes, Class<? extends Annotation> annotation) throws Exception{
        List<Class<?>> ret = new ArrayList<>();
        for(Class<?> clazz : classes){
            if(clazz.isAnnotationPresent(annotation)){
                ret.add(clazz);
            }
        }
        return ret;
    }
    
    public static List<Class<?>> getAnnotedMethods(List<Class<?>> classes, Class<? extends Annotation> annotation) throws Exception{
        List<Class<?>> ret = new ArrayList<>();
        for(Class<?> clazz : classes){
            Method[] m = clazz.getDeclaredMethods();
            if(clazz.isAnnotationPresent(annotation)){
                ret.add(clazz);
            }
        }
        return ret;
    }

    public static Map<UrlMethod, Mapping> getUrlMapping(List<Class<?>> controllers) throws UrlMappingException {
        Map<UrlMethod, Mapping> mapping = new HashMap<>();
        for (Class<?> controller : controllers) {
            for (Method methode : controller.getDeclaredMethods()) {
                if (methode.isAnnotationPresent(UrlMapping.class)) {
                    UrlMapping annotation = methode.getAnnotation(UrlMapping.class);
                    UrlMethod cle = new UrlMethod(annotation.url(), annotation.method());
                    if (mapping.containsKey(cle)) {
                        throw new UrlMappingException(
                            "Mapping duplique pour " + annotation.method() + " " + annotation.url()
                            + " (deja defini par " + mapping.get(cle).getController().getName()
                            + "." + mapping.get(cle).getMethode().getName() + ")"
                        );
                    }
                    mapping.put(cle, new Mapping(controller, methode));
                }
            }
        }
        return mapping;
    }
}
