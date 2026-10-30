package red.mvc.exception;

public class NotAuthorizedException extends Exception{
    
    @Override 
    public String getMessage(){
        return "Role non autorise";
    }
}
