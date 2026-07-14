package red.mvc.Utils;

import java.util.Objects;

public class UrlMethod {
    private String url;
    private String methode;

    public UrlMethod() {
    }

    public UrlMethod(String url, String methode) {
        this.url = url;
        this.methode = methode;
    }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getMethode() { return methode; }
    public void setMethode(String methode) { this.methode = methode; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof UrlMethod)) return false;
        UrlMethod other = (UrlMethod) obj;
        return Objects.equals(this.url, other.url) && Objects.equals(this.methode, other.methode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, methode);
    }

    @Override
    public String toString() {
        return methode + " " + url;
    }
}
