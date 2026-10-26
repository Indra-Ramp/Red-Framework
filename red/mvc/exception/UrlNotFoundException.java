package red.mvc.exception;

import java.util.Map;

import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;

public class UrlNotFoundException extends Exception {
    private Map<UrlMethod, Mapping> availableUrls;
    private UrlMethod intendedUrl;

    public UrlMethod getIntendedUrl() {
        return intendedUrl;
    }

    public void setIntendedUrl(UrlMethod intendedUrl) {
        this.intendedUrl = intendedUrl;
    }

    public Map<UrlMethod, Mapping> getAvailableUrls() {
        return availableUrls;
    }

    private String buildMessage() {
        StringBuffer buff = new StringBuffer();
        buff.append("URL: ").append(intendedUrl).append(" not found. Here are the available URLS:\n");
        for(Map.Entry<UrlMethod, Mapping> entry : availableUrls.entrySet()) {
            buff.append(entry.getKey()).append(": ").append("Controller: ").append(entry.getValue().getController().getName());
            buff.append(", Method: ").append(entry.getValue().getMethod().getName()).append("\n");
        }
        return buff.toString();
    }

    public void setAvailableUrls(Map<UrlMethod, Mapping> availableUrls) {
        this.availableUrls = availableUrls;
    }

    public UrlNotFoundException() {

    }

    @Override
    public String getMessage() {
        return buildMessage();
    }

    public UrlNotFoundException(Map<UrlMethod, Mapping> available, UrlMethod intendedUrl) {
        setAvailableUrls(available);
        setIntendedUrl(intendedUrl);
    }

}

