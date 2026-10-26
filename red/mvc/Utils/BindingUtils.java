package red.mvc.Utils;

public class BindingUtils {
    public static record PathInfo(String fieldName, int index) {
        public boolean isList() {
            return index != -1;
        }
    }

    private static final java.util.regex.Pattern FIELD_AND_INDEX_PATTERN = java.util.regex.Pattern
            .compile("(\\w+)(?:\\[(\\d+)\\])?");

    public static PathInfo parsePathSegment(String pathSegment) {
        if (pathSegment == null) {
            return new PathInfo(null, -1);
        }

        java.util.regex.Matcher matcher = FIELD_AND_INDEX_PATTERN.matcher(pathSegment);
        if (matcher.matches()) {
            String fieldName = matcher.group(1);
            String indexStr = matcher.group(2); 

            int index = (indexStr != null) ? Integer.parseInt(indexStr) : -1;
            return new PathInfo(fieldName, index);
        }

        return new PathInfo(pathSegment, -1); 
    }

}
