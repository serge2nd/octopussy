package ru.serge2nd.util.immutable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.UnaryOperator;

import static ru.serge2nd.util.TypeWrap.rawClass;

public interface ImmutableProviderByClass<T> extends ImmutableProvider<T> {

    Optional<UnaryOperator<T>> wrap(Class<T> clazz);

    @Override
    @SuppressWarnings("unchecked")
    default Optional<UnaryOperator<T>> wrap(Type type) {
        Class<?> raw = rawClass(type);
        return this.wrap((Class<T>)raw);
    }
}
