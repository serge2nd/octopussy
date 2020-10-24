package ru.serge2nd.octopussy.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(Lifecycle.PER_CLASS)
class QueryWithParamsTest {
    static final String Q = "xyz";
    static final Map<String, Object> PARAMS = new HashMap<>(singletonMap("nine", 9));

    @Test void testInstance() {
        // WHEN
        QueryWithParams q = new QueryWithParams(Q, PARAMS);

        /* THEN */ assertAll(() ->
        assertSame(Q, q.getQuery(), "expected same query"), () ->
        assertEquals(PARAMS, q.getParams(), "expected same params"), () ->
        assertThrows(UnsupportedOperationException.class, ()->q.getParams().clear(), "expected unmodifiable params"));
    }

    @Test void testNullParams() {
        // WHEN
        QueryWithParams q = new QueryWithParams(Q, null);

        /* THEN */ assertAll(() ->
        assertSame(Q, q.getQuery(), "expected same query"), () ->
        assertSame(emptyMap(), q.getParams(), "expected empty map"));
    }

    @Test void testNullQuery() { assertThrows(IllegalArgumentException.class, ()->new QueryWithParams(null, PARAMS)); }
}