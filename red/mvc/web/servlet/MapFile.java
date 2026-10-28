package red.mvc.web.servlet;

import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

public class MapFile {
    private String fileName;
    private String ext;
    private byte[] bytes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    @Override 
    public boolean equals(Object other) {
        if(other == null || !this.getClass().equals(other.getClass())) {
            return false;
        }
        MapFile o = (MapFile) other;
        boolean matchName = getFileName().equals(o.getFileName());
        boolean matchExt = getExt().equals(o.getExt());
        return matchName && matchExt;
    }

    @Override 
    public int hashCode() {
        return Objects.hash(getFileName(), getExt());
    }

    public void save(String path) throws Exception {
        String fullPath = path + "/" + getFileName() + "." + getExt();
        Files.write(Path.of(fullPath), bytes, StandardOpenOption.CREATE);
    }
}
