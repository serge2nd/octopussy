package ru.serge2nd.octopussy.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static ru.serge2nd.octopussy.util.Queries.EMPTY;
import static ru.serge2nd.octopussy.util.Queries.mapToQueries;
import static ru.serge2nd.octopussy.util.Queries.queries;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.AssertForMany.assertForMany;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CommonMatch.equalTo;
import static ru.serge2nd.test.match.CommonMatch.illegalArgument;
import static ru.serge2nd.test.match.CommonMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class QueriesTest {
    static final QueryWithParams[] QS = {new QueryWithParams("abc", null), new QueryWithParams("xyz", singletonMap("nine", 9))};
    static final List<QueryWithParams> QL = asList(QS);

    @Test void testQueriesOfArray() { assertThat(queries(QS), instanceOf(UnmodifiableArrayQueries.class), equalTo(QL)); }
    @Test void testNoQueries()      { assertThat(queries(), sameAs(EMPTY)); }

    @Test void testQueriesOfList()                     { assertThat(queries(QL), instanceOf(UnmodifiableQueries.class), equalTo(QL)); }
    @Test void testQueriesOfEmptyList()                { assertThat(queries(emptyList()), sameAs(EMPTY)); }
    @Test void testQueriesOfUnmodifiableQueries()      { Queries qs = queries(QL); assertThat(queries(qs), sameAs(qs)); }
    @Test void testQueriesOfUnmodifiableArrayQueries() { Queries qs = queries(QS); assertThat(queries(qs), sameAs(qs)); }

    @Test void testMapToQueries() {
        String[] strs = {"s0", "s1"};
        assertEquals(
            stream(strs).map(s -> new QueryWithParams(s, null)).collect(toList()),
            collect(strs, mapToQueries(s -> new QueryWithParams(s, null), strs.length)),
            "expected proper collecting result");
    }

    @SuppressWarnings("ConstantConditions")
    @Test void testNullArgs() {
        assertForMany(illegalArgument(),
        () -> queries((QueryWithParams[])null),
        () -> queries((List<QueryWithParams>)null),
        () -> mapToQueries(null, 0));
    }
}