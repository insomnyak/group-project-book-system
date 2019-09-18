package com.company.noteservice.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

public class MapClasses<First, Second> {
    private First f;
    private Second s;

    public MapClasses(First f, Second s) {
        this.f = f;
        this.s = s;
    }

    public MapClasses(First f, Class<Second> extendedClass) {
        this.f = f;
        try {
            this.s = extendedClass.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException(e.getMessage() + " | " + e.getCause());
        }
    }

    public Second mapFirstToSecond(boolean ignoreErrors) {
        List<Field> fFields = getFields(f.getClass(), new ArrayList<>());
        List<Field> sFields = getFields(s.getClass(), new ArrayList<>());

        fFields = fFields.stream().filter(field ->
                sFields.stream().anyMatch(field1 -> field1.getName().equals(field.getName())))
                .collect(Collectors.toList());

        if (fFields.isEmpty()) return s;

        for (Field field : fFields) {
            try {
                Class<?>[] noParams = {};
                String name = field.getName().substring(0,1).toUpperCase() +
                        field.getName().substring(1);
                String setMethod = "set" + name;
                String getMethod = "get" + name;
                Method getF = f.getClass().getMethod(getMethod, noParams);
                Method getS = s.getClass().getMethod(getMethod, noParams);
                Method setS = setMethod(s.getClass(), setMethod, getF.getGenericReturnType());
                getF.setAccessible(true);
                setS.setAccessible(true);
                setS.invoke(s, getF.invoke(f));
            } catch (NoSuchMethodException e) {
                if (!ignoreErrors) {
                    throw new NoSuchElementException("Method not found. " + e.getMessage());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (!ignoreErrors) {
                    throw new RuntimeException("Attempting to invoke a method that's not accessible. " +
                            e.getMessage() + " | " + e.getCause());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error occurred. \n" +
                        e.getMessage() + " | " + e.getCause());
            }
        }
        return s;
    }

    public Second mapFirstToSecond(boolean ignoreErrors, boolean ignoreNonNullsInSecond) {
        List<Field> fFields = getFields(f.getClass(), new ArrayList<>());
        List<Field> sFields = getFields(s.getClass(), new ArrayList<>());

        fFields = fFields.stream().filter(field ->
                sFields.stream().anyMatch(field1 -> field1.getName().equals(field.getName())))
                .collect(Collectors.toList());

        if (fFields.isEmpty()) return s;

        for (Field field : fFields) {
            try {
                Class<?>[] noParams = {};
                String name = field.getName().substring(0,1).toUpperCase() +
                        field.getName().substring(1);
                String setMethod = "set" + name;
                String getMethod = "get" + name;
                Method getF = f.getClass().getMethod(getMethod, noParams);
                Method getS = s.getClass().getMethod(getMethod, noParams);
                Method setS = setMethod(s.getClass(), setMethod, getF.getGenericReturnType());
                getF.setAccessible(true);
                setS.setAccessible(true);
                if (!ignoreNonNullsInSecond) {
                    setS.invoke(s, getF.invoke(f));
                } else if (getS.invoke(s) == null) {
                    setS.invoke(s, getF.invoke(f));
                }
            } catch (NoSuchMethodException e) {
                if (!ignoreErrors) {
                    throw new NoSuchElementException("Method not found. " + e.getMessage());
                }
            } catch (IllegalAccessException | InvocationTargetException e) {
                if (!ignoreErrors) {
                    throw new RuntimeException("Attempting to invoke a method that's not accessible. " +
                            e.getMessage() + " | " + e.getCause());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error occurred. \n" +
                        e.getMessage() + " | " + e.getCause());
            }
        }
        return s;
    }

    public List<Field> getFields(Class className, List<Field> fields) {
        if (className == Object.class) return fields;
        Field[] f = className.getDeclaredFields();
        fields.addAll(Arrays.asList(f));
        getFields(className.getSuperclass(), fields);
        return fields;
    }

    public Method setMethod(Class<?> className, String methodName, Type returnType) {
        try {
            return className.getMethod(methodName, (Class<?>) returnType);
        } catch (NoSuchMethodException ex) {
            if (className != Object.class) {
                return setMethod(className.getSuperclass(), methodName, returnType);
            } else {
                throw new RuntimeException(ex.getMessage() + " | " + ex.getCause().toString());
            }
        }
    }
}
