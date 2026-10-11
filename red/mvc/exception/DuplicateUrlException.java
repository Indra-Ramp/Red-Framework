package red.mvc.exception;

import red.mvc.Utils.Mapping;
import red.mvc.Utils.UrlMethod;

public class DuplicateUrlException extends Exception {
    private Mapping existing;
    private Mapping intended;
    private UrlMethod url;
    public UrlMethod getUrl() {
        return url;
    }
    public void setUrl(UrlMethod url) {
        this.url = url;
    }
    public Mapping getExisting() {
        return existing;
    }
    public void setExisting(Mapping existing) {
        this.existing = existing;
    }
    public Mapping getIntended() {
        return intended;
    }
    public void setIntended(Mapping intended) {
        this.intended = intended;
    }

    @Override
    public String getMessage() {
        StringBuffer sb = new StringBuffer();
        sb.append("Duplicate URL: ").append(url).append("\n");
        sb.append("Already declared in ").append(existing.getController().getName()).append(", Method: ").append(existing.getMethod().getName()).append("\n");
        sb.append("Redeclared in ").append(intended.getController().getName()).append(", Method: ").append(intended.getMethod().getName()).append("\n");
        return sb.toString();
    }

}
