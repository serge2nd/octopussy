package ru.serge2nd.util.immutable;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.UnaryOperator;

public interface ImmutableProvider<T> {

    Optional<UnaryOperator<T>> wrap(Type type);
}
