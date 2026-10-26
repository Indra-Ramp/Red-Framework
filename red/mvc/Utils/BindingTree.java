package red.mvc.Utils;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BindingTree {
    private final BindingNode root = new BindingNode("root");

    public static class BindingNode {
        String propertyName;
        String[] value;
        Class<?> type;
        Type genericType;
        boolean list = false;
        boolean map = false;
        public boolean isMap() {
            return map;
        }

        public void setMap(boolean map) {
            this.map = map;
        }

        Map<String, BindingNode> children = new HashMap<>();

        BindingNode(String propertyName) {
            this.propertyName = propertyName;
        }

        BindingNode(String propertyName, Class<?> type) {
            this.propertyName = propertyName;
            this.type = type;
        }

        BindingNode(String propertyName, Class<?> type, Type genericType) {
            this.propertyName = propertyName;
            this.type = type;
            this.genericType = genericType;
        }

        @Override 
        public boolean equals(Object o) {
            if(!o.getClass().equals(this.getClass())) {
                return false;
            }
            BindingNode node = (BindingNode) o;
            return node.propertyName == propertyName;
        }

        @Override 
        public int hashCode() {
            return Objects.hash(propertyName, value, type, genericType, list);
        }

        public String toString() {
            StringBuffer sb = new StringBuffer();
            sb.append("PROPERTY: ").append(propertyName).append(" ; VALUE: ").append(value).append("\n");
            sb.append("CHILDREN: ").append(children);
            return sb.toString();
        }
    }

    private static final Pattern INDEX_PATTERN = Pattern.compile("\\w+\\[(\\d+)\\]");

    public static int getListIndex(String pathSegment) {
        if (pathSegment == null) {
            return -1;
        }
        Matcher matcher = INDEX_PATTERN.matcher(pathSegment);
        if (matcher.matches()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }

    public BindingNode getRoot() {
        return root;
    }

    public void insertPath(String path, String[] value) {
        String[] parts = path.split("\\.");
        BindingNode current = root;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            current.children.putIfAbsent(part, new BindingNode(part));
            current = current.children.get(part);
            if (i == parts.length - 1) {
                current.value = value;
            }
        }
    }
}
