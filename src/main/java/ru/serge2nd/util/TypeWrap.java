package ru.serge2nd.util;

import com.fasterxml.jackson.databind.type.TypeFactory;
import lombok.Getter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Objects;

public final class TypeWrap<T> implements ParameterizedType {
    @Getter
    private final Type type;
    private final Class<T> raw;

    public static <U> TypeWrap<U> of(Class<U> clazz, Type... typeParams) {
        if (typeParams.length == 0)
            return new TypeWrap<>(clazz, clazz);
        return of(clazz, typeParams, null);
    }

    public static <U> TypeWrap<U> of(Class<U> clazz, Type[] typeParams, Type owner) {
        typeParams = Arrays.stream(typeParams)
                .map(TypeWrap::unwrap)
                .toArray(Type[]::new);
        return new TypeWrap<>(makeParameterizedType(clazz, typeParams, unwrap(owner)), clazz);
    }

    private TypeWrap(Type type, Class<T> raw) {
        this.type = type;
        this.raw = raw == null ? rawClass(type) : raw;
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

    public static ParameterizedType makeParameterizedType(Class<?> raw, Type[] typeParams, Type owner) {
        return new ParameterizedType() {
            public Type getRawType() { return raw; }
            public Type[] getActualTypeArguments() { return typeParams; }
            public Type getOwnerType() { return owner; }

            public boolean equals(Object o) {
                if (this == o) return true;
                if (!(o instanceof ParameterizedType)) return false;
                ParameterizedType other = (ParameterizedType) o;

                return Objects.equals(getRawType(), other.getRawType()) &&
                        Arrays.equals(getActualTypeArguments(), other.getActualTypeArguments()) &&
                        Objects.equals(getOwnerType(), other.getOwnerType());
            }

            public int hashCode() {
                return Arrays.hashCode(typeParams) ^
                        Objects.hashCode(owner) ^
                        Objects.hashCode(raw);
            }
        };
    }
}
