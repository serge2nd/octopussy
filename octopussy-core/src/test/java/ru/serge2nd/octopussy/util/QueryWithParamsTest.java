package ru.serge2nd.octopussy.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.match.CoreMatch.equalTo;
import static ru.serge2nd.test.match.CoreMatch.fails;
import static ru.serge2nd.test.match.CoreMatch.illegalArgument;
import static ru.serge2nd.test.match.CoreMatch.sameAs;

@TestInstance(Lifecycle.PER_CLASS)
class QueryWithParamsTest {
    static final String Q = "xyz";
    static final Map<String, Object> PARAMS = new HashMap<>(singletonMap("nine", 9));

    @Test void testInstance() {
        // WHEN
        QueryWithParams q = new QueryWithParams(Q, PARAMS);

        /* THEN */ assertThat(
        q.getQuery()             , sameAs(Q),
        q.getParams()            , equalTo(PARAMS),
        ()->q.getParams().clear(), fails(UnsupportedOperationException.class));
    }

    @Test void testNullParams() {
        // WHEN
        QueryWithParams q = new QueryWithParams(Q, null);

        /* THEN */ assertThat(
        q.getQuery() , sameAs(Q),
        q.getParams(), sameAs(emptyMap()));
    }

    @Test void testNullQuery() { assertThat(()->new QueryWithParams(null, PARAMS), illegalArgument()); }
}