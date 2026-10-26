package red.mvc.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import red.mvc.Utils.BindingTree.BindingNode;
import red.mvc.Utils.BindingUtils.PathInfo;

public class BindingContext {
    // Une Map qui associe le nom de l'objet racine à son arbre de binding
    // personnalisé
    private final Map<String, BindingTree.BindingNode> roots = new HashMap<>();

    public Map<String, BindingTree.BindingNode> getRoots() {
        return roots;
    }

    private final Map<String, Class<?>> rootTypes = new HashMap<>();

    private final Map<String, Type> rootGenericType = new HashMap<>();

    public void registerRootType(String rootName, Class<?> clazz, Type genericType) {
        rootTypes.put(rootName, clazz);
        rootGenericType.put(rootName, genericType);
    }

    public void insertFormValue(String fullPath, String[] value) throws NoSuchFieldException {
        String[] parts = fullPath.split("\\.", 2);
        String rootName = parts[0];
        PathInfo pi = BindingUtils.parsePathSegment(rootName);
        String name = pi.fieldName();

        Class<?> rootClass = rootTypes.get(name);
        if (rootClass == null) {
            return;
        }
        roots.putIfAbsent(name, new BindingTree.BindingNode(name, rootClass, rootGenericType.get(name)));

        BindingNode rootNode = roots.get(name);

        if (pi.isList()) {
            rootNode.list = true;
            Class<?> listClass = null;
            Type genericType = rootGenericType.get(name);
            if (genericType instanceof ParameterizedType pt) {
                listClass = (Class<?>) pt.getActualTypeArguments()[0];
                // System.out.println("CLASS:" + listClass);
            }
            String idx = String.valueOf(pi.index());
            rootNode.children.putIfAbsent(idx, new BindingNode(idx, listClass, null));
            rootNode = rootNode.children.get(idx);
        }

        if (parts.length > 1) {
            insertPathInNode(rootNode, parts[1], value);
        } else {
            rootNode.value = value;
        }
    }

    public String[] insertAt(String[] originalArray, String newValue, int index) {
        if (index < 0 || index > originalArray.length) {
            throw new IndexOutOfBoundsException("Index invalide : " + index);
        }

        List<String> list = new ArrayList<>(Arrays.asList(originalArray));

        list.add(index, newValue);

        return list.toArray(new String[0]);
    }

    public static boolean isNumeric(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void insertPathInNode(BindingTree.BindingNode currentNode, String remainingPath, String[] value)
            throws NoSuchFieldException {
        String[] parts = remainingPath.split("\\.");
        Class<?> currentClass = currentNode.type;
        // if(currentClass == null) {
        //     return;
        // }
        int actualPart = parts.length;
        for (int i = 0; i < actualPart; i++) {
            String part = parts[i];
            Class<?> fieldType = null;
            Type genericType = null;
            Field found = null;
            PathInfo pi = BindingUtils.parsePathSegment(part);

            int index = pi.index();
            String name = pi.fieldName();
            if (currentClass != null && !isNumeric(part)) {
                try {
                    found = currentClass.getDeclaredField(name);
                    fieldType = found.getType();
                    genericType = found.getGenericType();
                } catch (NoSuchFieldException e) {
                    throw e;
                }
            }

            currentNode.children.putIfAbsent(name,
                    new BindingTree.BindingNode(name, fieldType, genericType));
            currentNode = currentNode.children.get(name);
            if (pi.isList()) {
                currentNode.list = true;
                Class<?> listClass = null;
                if (genericType instanceof ParameterizedType pt) {
                    listClass = (Class<?>) pt.getActualTypeArguments()[0];
                    // System.out.println("CLASS:" + listClass);
                }
                String indexStr = String.valueOf(index);
                currentNode.children.putIfAbsent(indexStr, new BindingNode(indexStr, listClass, null));
                currentNode = currentNode.children.get(indexStr);
                currentClass = listClass;
                // parts = insertAt(parts, indexStr, i + 1);
                // actualPart++;
            } else {
                currentClass = fieldType;
            }
            if (i == parts.length - 1) {
                currentNode.value = value;
                System.out.println("VALUES VAOVAO: " + value[0]);
                // System.out.println("FARANY: " + currentNode.propertyName + " = " + value);
            }
        }
    }

    public Object instantiateTree(BindingNode node) throws Exception {
        if (node.children.isEmpty()) {
            System.out.println("ETO> " + node.propertyName + " , " + node.type);
            return TypeConverter.convert(node.value, node.type, node.genericType);
        }
        Object instance = null;
        List<Object> list = null;
        Map<String, Object> map = null;
        if (node.list) {
            list = new ArrayList<>();
            instance = list;
        } else if (node.isMap()) {
            map = new HashMap<>();
        } else {
            instance = node.type.getDeclaredConstructor().newInstance();
        }
        for (BindingNode child : node.children.values()) {
            if (node.list) {
                int targetIdx = Integer.parseInt(child.propertyName);
                while (list.size() <= targetIdx) {
                    list.add(null);
                }
                list.set(targetIdx, instantiateTree(child));
                continue;
            }
            if (node.map) {
                String key = child.propertyName;
                map.put(key, instantiateTree(child));
                continue;
            }
            Object childValue = instantiateTree(child);
            Field field = node.type.getDeclaredField(child.propertyName);
            String propertyName = child.propertyName;
            String setterName = "set" + Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
            Method setter = node.type.getDeclaredMethod(setterName, field.getType());
            setter.setAccessible(true);
            setter.invoke(instance, childValue);
        }

        return instance;
    }

    public Map<String, Object> instantiateAll() throws Exception {
        Map<String, Object> instances = new HashMap<>();
        for (Map.Entry<String, BindingTree.BindingNode> entry : roots.entrySet()) {
            Object instance = instantiateTree(entry.getValue());
            instances.put(entry.getKey(), instance);
        }
        return instances;
    }
}
