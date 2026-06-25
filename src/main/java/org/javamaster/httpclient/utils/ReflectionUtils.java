package org.javamaster.httpclient.utils;

import org.apache.http.HttpStatus;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @author yudong
 */
public class ReflectionUtils {

    public static Field findField(Class<?> clazz, String name) {
        for (Class<?> searchType = clazz; searchType != Object.class && searchType != null; searchType = searchType.getSuperclass()) {
            try {
                Field field = searchType.getDeclaredField(name);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
            }
        }

        return null;
    }

    public static Method findFirstNoArgMethod(Class<?> clazz) {
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.getParameterCount() == 0) {
                method.setAccessible(true);
                return method;
            }
        }

        return null;
    }

    public static Method findMethod(Class<?> clazz, String name, Class<?>... paramTypes) {
        for (Class<?> searchType = clazz; searchType != null; searchType = searchType.getSuperclass()) {
            try {
                Method method = searchType.isInterface() ? searchType.getMethod(name, paramTypes) : searchType.getDeclaredMethod(name, paramTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }

        return null;
    }

    public static String findStatusDesc(int statusCode) {
        try {
            Class<HttpStatus> clz = HttpStatus.class;
            Field[] declaredFields = clz.getDeclaredFields();
            for (Field declaredField : declaredFields) {
                declaredField.setAccessible(true);
                int code = (int) declaredField.get(null);
                if (code != statusCode) {
                    continue;
                }

                String name = declaredField.getName();
                return name.replace("SC_", "");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return "UNKNOWN";
    }

}
