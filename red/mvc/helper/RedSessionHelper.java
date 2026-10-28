package red.mvc.helper;

import java.util.Enumeration;
import java.util.Set;

import red.mvc.web.servlet.RedSession;
import jakarta.servlet.http.HttpSession;

public class RedSessionHelper {
    public static void fillWithSession(RedSession session, HttpSession httpSession) {
        session.setCreationTime(httpSession.getCreationTime());
        session.setLastAccessedTime(httpSession.getLastAccessedTime());
        session.setMaxInactiveInterval(httpSession.getMaxInactiveInterval());
        Enumeration<String> attributesName = httpSession.getAttributeNames();
        while(attributesName.hasMoreElements()) {
            String attr = attributesName.nextElement();
            session.set(attr, httpSession.getAttribute(attr));
        }
    }

    public static void overrideHttpSession(RedSession session, HttpSession httpSession) {
        Set<String> keys = session.getAttributesNames();
        for(String key : keys) {
            httpSession.setAttribute(key, session.get(key));
        }
    }
}
