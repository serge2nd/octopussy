package ru.serge2nd.octopussy.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.function.Executable;

import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.*;
import static ru.serge2nd.octopussy.util.Queries.*;
import static ru.serge2nd.stream.util.Collecting.collect;

@TestInstance(Lifecycle.PER_CLASS)
class QueriesTest {
    static final QueryWithParams[] QS = {new QueryWithParams("abc", null), new QueryWithParams("xyz", singletonMap("nine", 9))};
    static final List<QueryWithParams> QL = asList(QS);

    @Test void testQueriesOfArray() {
        // WHEN
        Queries qs = queries(QS);

        /* THEN */ assertAll(() ->
        assertTrue(qs instanceof UnmodifiableArrayQueries, "expected unmodifiable queries"), () ->
        assertEquals(QL, qs, "expected same queries"));
    }
    @Test void testNoQueries() { assertSame(EMPTY, queries()); }

    @Test
    void testQueriesOfList() {
        // WHEN
        Queries qs = queries(QL);

        /* THEN */ assertAll(() ->
        assertTrue(qs instanceof UnmodifiableQueries, "expected unmodifiable queries"), () ->
        assertEquals(QL, qs, "expected same queries"));
    }
    @Test void testQueriesOfEmptyList()                { assertSame(EMPTY, queries(emptyList())); }
    @Test void testQueriesOfUnmodifiableQueries()      { Queries qs = queries(QL); assertSame(qs, queries(qs)); }
    @Test void testQueriesOfUnmodifiableArrayQueries() { Queries qs = queries(QS); assertSame(qs, queries(qs)); }

    @Test void testMapToQueries() {
        String[] strs = {"s0", "s1"};
        assertEquals(
            stream(strs).map(s -> new QueryWithParams(s, null)).collect(toList()),
            collect(strs, mapToQueries(s -> new QueryWithParams(s, null), strs.length)),
            "expected proper collecting result");
    }

    @SuppressWarnings("ConstantConditions")
    @Test void testNullArgs() { assertAll(Stream.<Executable>of(
        () -> queries((QueryWithParams[])null),
        () -> queries((List<QueryWithParams>)null),
        () -> mapToQueries(null, 0)
        ).map(e -> ()->assertThrows(IllegalArgumentException.class, e)));
    }
}