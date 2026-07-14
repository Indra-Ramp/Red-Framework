package red.mvc.exception;

public class UrlMappingException extends Exception {

    public UrlMappingException(String message) {
        super(message);
    }

    public UrlMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}