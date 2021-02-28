package ru.serge2nd.octopussy.util;

import lombok.NonNull;
import ru.serge2nd.collection.UnmodifiableArrayList;
import ru.serge2nd.collection.UnmodifiableList;
import ru.serge2nd.stream.util.Accumulators.SuppliedAccumulator;

import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static ru.serge2nd.stream.util.ArrayAccumulators.mapping;
import static ru.serge2nd.stream.util.Collecting.noCombiner;

/**
 * A list of {@link QueryWithParams} objects.
 */
public interface Queries extends List<QueryWithParams> {
    /**
     * Constructs unmodifiable queries from the specified array.
     * Note that changes to the original array reflect this queries.
     * @param queries array to be wrapped by {@link Queries}
     * @return unmodifiable {@link Queries}
     */
    static Queries queries(@NonNull QueryWithParams... queries) {
        if (queries.length == 0) return EMPTY;
        return new UnmodifiableArrayQueries(queries);
    }

    /**
     * Constructs unmodifiable queries from the specified list.
     * Note that changes to the original list reflect this queries.
     * @param queries list to be wrapped by {@link Queries}
     * @return unmodifiable {@link Queries}
     */
    @SuppressWarnings("rawtypes")
    static Queries queries(@NonNull List<? extends QueryWithParams> queries) {
        if ((List)emptyList() == queries) return EMPTY;
        if (queries instanceof UnmodifiableQueries || queries instanceof UnmodifiableArrayQueries)
            return (Queries)queries;
        return new UnmodifiableQueries(queries);
    }

    static <E> Collector<E, ?, Queries> mapToQueries(@NonNull Function<E, QueryWithParams> mapping, int len) {
        return new SuppliedAccumulator<E, QueryWithParams[], Queries>(() -> new QueryWithParams[len], mapping(mapping)) {
            @Override public Function<QueryWithParams[], Queries> finisher()     { return Queries::queries; }
            @Override public Set<Characteristics>              characteristics() { return emptySet(); }
            @Override public BinaryOperator<QueryWithParams[]>    combiner()     { return noCombiner(); }
        };
    }

    Queries EMPTY = new UnmodifiableQueries(emptyList());
}

final class UnmodifiableQueries extends UnmodifiableList<QueryWithParams> implements Queries {
    UnmodifiableQueries(List<? extends QueryWithParams> list) { super(list); }
}

final class UnmodifiableArrayQueries extends UnmodifiableArrayList<QueryWithParams> implements Queries {
    UnmodifiableArrayQueries(QueryWithParams[] queries) { super(queries); }
}
