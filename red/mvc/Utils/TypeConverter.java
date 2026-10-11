package red.mvc.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TypeConverter {
    public static Object convert(Object rawValue, Class<?> targetType, Type genericType) {
        if (rawValue == null) {
            return null;
        }
        if (targetType.isArray()) {
            String[] values = toStringArray(rawValue);
            Class<?> componentType = targetType.getComponentType();
            Object array = Array.newInstance(componentType, values.length);
            
            for (int i = 0; i < values.length; i++) {
                Array.set(array, i, convertScalar(values[i], componentType));
            }
            return array;
        }
        if (List.class.isAssignableFrom(targetType)) {
            String[] values = toStringArray(rawValue);
            List<Object> list = new ArrayList<>();
            
            Class<?> itemType = String.class; 
            if (genericType instanceof ParameterizedType) {
                ParameterizedType pt = (ParameterizedType) genericType;
                itemType = (Class<?>) pt.getActualTypeArguments()[0];
            }

            for (String val : values) {
                list.add(convertScalar(val, itemType));
            }
            return list;
        }

        String singleValue = (rawValue instanceof String[]) ? ((String[]) rawValue)[0] : rawValue.toString();
        return convertScalar(singleValue, targetType);
    }

    private static String[] toStringArray(Object rawValue) {
        if (rawValue instanceof String[]) {
            return (String[]) rawValue;
        } else if (rawValue instanceof String) {
            return new String[] { (String) rawValue };
        }
        return new String[] { rawValue.toString() };
    }

    private static Object convertScalar(String value, Class<?> targetClass) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        String trimmed = value.trim();

        if (targetClass.equals(String.class)) return trimmed;
        if (targetClass.equals(Integer.class) || targetClass.equals(int.class)) return Integer.valueOf(trimmed);
        if (targetClass.equals(Long.class) || targetClass.equals(long.class)) return Long.valueOf(trimmed);
        if (targetClass.equals(Double.class) || targetClass.equals(double.class)) return Double.valueOf(trimmed);
        if (targetClass.equals(Boolean.class) || targetClass.equals(boolean.class)) return Boolean.valueOf(trimmed);
        if (targetClass.equals(UUID.class)) return UUID.fromString(trimmed);

        throw new IllegalArgumentException("Type non supporté : " + targetClass.getName());
    }
}