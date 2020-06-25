package ru.serge2nd.util;

import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;
import sun.reflect.generics.reflectiveObjects.ParameterizedTypeImpl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

public final class TypeWrap<T> implements ParameterizedType {
    @Getter
    private final Type type;
    private final Class<T> raw;

    public static <U> TypeWrap<U> of(Class<U> clazz, Type... typeParams) {
        if (typeParams.length == 0)
            return new TypeWrap<>(clazz);
        return of(clazz, typeParams, null);
    }

    public static <U> TypeWrap<U> of(Class<U> clazz, Type[] typeParams, Type ownerType) {
        typeParams = Arrays.stream(typeParams)
                .map(TypeWrap::unwrap)
                .toArray(Type[]::new);
        return new TypeWrap<>(ParameterizedTypeImpl.make(clazz, typeParams, unwrap(ownerType)));
    }

    private TypeWrap(Type type) {
        this.type = type;
        this.raw = rawClass(type);
    }

    public boolean isClass() { return type == raw; }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (obj.getClass() != this.getClass())
            return false;

        TypeWrap<?> otherType = (TypeWrap<?>) obj;

        return type.equals(otherType.type);
    }

    @Override
    public int hashCode() { return type.hashCode(); }
    @Override
    public String getTypeName() { return type.getTypeName(); }
    @Override
    public String toString() { return type.toString(); }

    @Override
    public Type[] getActualTypeArguments() {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType)type).getActualTypeArguments();
        return new Type[0];
    }

    @Override
    public Class<T> getRawType() { return raw; }

    @Override
    public Type getOwnerType() {
        if (type instanceof ParameterizedType)
            return ((ParameterizedType)type).getOwnerType();
        return null;
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> rawClass(Type type) {
        return (Class<T>)TypeFactory.rawClass(unwrap(type));
    }

    public static Type unwrap(Type t) {
        return t instanceof TypeWrap ? ((TypeWrap<?>)t).getType() : t;
    }
}
