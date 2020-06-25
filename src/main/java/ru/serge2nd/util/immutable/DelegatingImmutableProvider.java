package ru.serge2nd.util.immutable;

import lombok.NonNull;
import ru.serge2nd.util.TypeWrap;

import java.lang.reflect.Type;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.UnaryOperator;

import static ru.serge2nd.util.TypeWrap.rawClass;

public class DelegatingImmutableProvider<T> implements ImmutableProvider<T> {
    private final Map<TypeWrap<? extends T>, ImmutableProvider<? extends T>> delegates = new HashMap<>();
    private final Deque<TypeWrap<? extends T>> orderedTypes = new LinkedList<>();

    @Override
    @SuppressWarnings("unchecked")
    public Optional<UnaryOperator<T>> wrap(@NonNull Type type) {
        return orderedTypes.stream()
                .filter(key -> Objects.equals(key.getType(), type) ||
                        key.getType() instanceof Class && key.getRawType().isAssignableFrom(rawClass(type)))
                .map(key -> (ImmutableProvider<T>) delegates.get(key))
                .map(p -> p.wrap(type))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }

    public <U extends T>
    void registerDelegate(@NonNull Class<U> clazz, @NonNull ImmutableProviderByClass<U> delegate) {
        this.registerDelegate(TypeWrap.of(clazz), delegate);
    }

    public <U extends T>
    void registerPrimaryDelegate(@NonNull Class<U> clazz, @NonNull ImmutableProviderByClass<U> delegate) {
        this.registerPrimaryDelegate(TypeWrap.of(clazz), delegate);
    }

    public <U extends T>
    void registerDelegate(@NonNull TypeWrap<U> type, @NonNull ImmutableProvider<U> delegate) {
        doRegister(type, delegate, Deque::addLast);
    }

    public <U extends T>
    void registerPrimaryDelegate(@NonNull TypeWrap<U> type, @NonNull ImmutableProvider<U> delegate) {
        doRegister(type, delegate, Deque::addFirst);
    }

    private void doRegister(TypeWrap<? extends T> type, ImmutableProvider<? extends T> delegate,
                            BiConsumer<Deque<TypeWrap<? extends T>>, TypeWrap<? extends T>> addOp) {
        addOp.accept(orderedTypes, type);
        delegates.put(type, delegate);
    }
}
