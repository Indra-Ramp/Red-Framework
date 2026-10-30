package red.mvc.exception;

public class AuthRequiredException extends Exception{
    
    @Override 
    public String getMessage(){
        return "Authentification required";
    }
}