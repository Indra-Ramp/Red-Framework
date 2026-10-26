package red.mvc.Utils;

import java.lang.reflect.Array;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public class TypeConverter {
    public static Object convert(Object rawValue, Class<?> targetType, Type genericType) {
        if (rawValue == null) {
            return null;
        }
        if (targetType.isArray()) {
            return handlePrimitiveArray(rawValue, targetType);
        }
        if (List.class.isAssignableFrom(targetType)) {
            return handlePrimitiveList(rawValue, genericType);
        }

        String singleValue = (rawValue instanceof String[]) ? ((String[]) rawValue)[0] : rawValue.toString();
        return ScalarConverter.convertScalar(singleValue, targetType);
    }

    private static Object handlePrimitiveArray(Object rawValue, Class<?> targetType) {
        String[] values = toStringArray(rawValue);
        Class<?> componentType = targetType.getComponentType();
        Object array = Array.newInstance(componentType, values.length);

        if(componentType.equals(byte.class)) {
            return Base64.getDecoder().decode(values[0]);
        }

        for (int i = 0; i < values.length; i++) {
            Array.set(array, i, ScalarConverter.convertScalar(values[i], componentType));
        }
        return array;
    }

    private static Object handlePrimitiveList(Object rawValue, Type genericType) {
        String[] values = toStringArray(rawValue);
        List<Object> list = new ArrayList<>();

        Class<?> itemType = String.class;
        if (genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) genericType;
            itemType = (Class<?>) pt.getActualTypeArguments()[0];
        }

        for (String val : values) {
            list.add(ScalarConverter.convertScalar(val, itemType));
        }
        return list;
    }

    private static String[] toStringArray(Object rawValue) {
        if (rawValue instanceof String[]) {
            return (String[]) rawValue;
        } else if (rawValue instanceof String) {
            return new String[] { (String) rawValue };
        }
        return new String[] { rawValue.toString() };
    }

    private class ScalarConverter {

        private static final Map<Class<?>, Function<String, ?>> CONVERTERS = new HashMap<>();

        static {
            // Tyh Chaîne
            CONVERTERS.put(String.class, s -> s);

            // Tyh Nombres entiers
            CONVERTERS.put(Integer.class, Integer::valueOf);
            CONVERTERS.put(int.class, Integer::valueOf);
            CONVERTERS.put(Long.class, Long::valueOf);
            CONVERTERS.put(long.class, Long::valueOf);
            CONVERTERS.put(Short.class, Short::valueOf);
            CONVERTERS.put(short.class, Short::valueOf);
            CONVERTERS.put(Byte.class, Byte::valueOf);
            CONVERTERS.put(byte.class, Byte::valueOf);
            CONVERTERS.put(BigInteger.class, BigInteger::new);

            // Tyh Nombres decimaux
            CONVERTERS.put(Double.class, Double::valueOf);
            CONVERTERS.put(double.class, Double::valueOf);
            CONVERTERS.put(Float.class, Float::valueOf);
            CONVERTERS.put(float.class, Float::valueOf);
            CONVERTERS.put(BigDecimal.class, BigDecimal::new);

            // Tyh Booleen sy Caractere
            CONVERTERS.put(Boolean.class, Boolean::valueOf);
            CONVERTERS.put(boolean.class, Boolean::valueOf);
            CONVERTERS.put(Character.class, s -> s.isEmpty() ? '\0' : s.charAt(0));
            CONVERTERS.put(char.class, s -> s.isEmpty() ? '\0' : s.charAt(0));

            // Tyh Identifiants uniques
            CONVERTERS.put(UUID.class, UUID::fromString);

            // Tyh Date et Heure (java.time)
            CONVERTERS.put(LocalDate.class, LocalDate::parse);
            CONVERTERS.put(LocalDateTime.class, LocalDateTime::parse);
            CONVERTERS.put(LocalTime.class, LocalTime::parse);
            CONVERTERS.put(Instant.class, Instant::parse);
        }

        public static Object convertScalar(String value, Class<?> targetClass) {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            String trimmed = value.trim();

            Function<String, ?> converter = CONVERTERS.get(targetClass);
            if (converter != null) {
                return converter.apply(trimmed);
            }

            if (targetClass.isEnum()) {
                @SuppressWarnings({ "unchecked", "rawtypes" })
                Class<Enum> enumClass = (Class<Enum>) targetClass;
                return Enum.valueOf(enumClass, trimmed);
            }

            throw new IllegalArgumentException("Type non supporté : " + targetClass.getName());
        }
    }
}