package ru.serge2nd.octopussy.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.MockServiceLayer;
import ru.serge2nd.octopussy.SpringBootSoftTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.test.util.Resources;

import javax.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.octopussy.AppContractsTesting.*;
import static ru.serge2nd.stream.MappingCollectors.mapToList;
import static ru.serge2nd.stream.util.Collecting.collect;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.util.CustomMatchers.equalToJson;

@SpringBootSoftTest
@ContextHierarchy({
    @ContextConfiguration(classes = TestWebConfig.class)
})
@MockServiceLayer
@TestInstance(Lifecycle.PER_CLASS)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class QueriesRqTest implements BaseContextTest {
    static final String J = str("query_rq_tmpl.json", Q, K1, V1, K2, V2);
    static final QueryRq RQ = query(Q, K1, V1);
    static final QueriesRq RQS = queryRqs(K2, V2, RQ);

    @Autowired ApplicationContext ctx;
    @Autowired JacksonTester<QueriesRq> tester;
    @Autowired Validator validator;

    static Stream<Arguments> validQueryRqProvider() { return Stream.of(
            arguments("query"            , queries(Q)),
            arguments("param"            , RQS),
            arguments("global param"     , queryRqs(K1, V1, query(Q))),
            arguments("null param"       , queryRqs(K1, V1, query(Q, K1, null))),
            arguments("null global param", queryRqs(K1, null, query(Q))),
            arguments("two queries"      , queryRqs(RQ, RQ))); }
    static Stream<Arguments> invalidQueryRqProvider() { return Stream.of(
            arguments("null queries"          , new QueriesRq(null, emptyMap())),
            arguments("no queries"            , queries()),
            arguments("null query"            , queries((String)null)),
            arguments("null query rq"         , queryRqs((QueryRq)null)),
            arguments("empty query"           , queryRqs(query(" \t\n"))),
            arguments("null param key"        , queryRqs(query(Q, null, V1))),
            arguments("null global param key" , queryRqs(null, V1, query(Q))),
            arguments("empty param key"       , queryRqs(query(Q, " \t\n", V1))),
            arguments("empty global param key", queryRqs(" \t\n", V1, query(Q)))); }

    @ParameterizedTest(name = "{0}")
    @MethodSource("validQueryRqProvider")
    void testValidQueryRq(String title, QueriesRq rq) {
        // WHEN
        int nViolations = validator.validate(rq).size();

        // THEN
        assertEquals(0, nViolations, "expected valid");
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidQueryRqProvider")
    void testInvalidQueryRq(String title, QueriesRq rq) {
        // WHEN
        int nViolations = validator.validate(rq).size();

        // THEN
        assertEquals(1, nViolations, "expected one violation");
    }

    @Test
    void testRead() throws IOException {
        // WHEN
        QueriesRq result = tester.read(new StringReader(J)).getObject();

        // THEN
        assertEquals(RQS, result, "read mismatches");
    }

    @Test
    void testWrite() throws IOException {
        assertThat(json(RQS), equalToJson(J));
    }

    static QueriesRq queries(String... qs) { return new QueriesRq(collect(qs, mapToList(QueriesRqTest::query, 0)), null); }

    static QueriesRq queryRqs(QueryRq... rqs) { return new QueriesRq(asList(rqs), null); }

    static QueriesRq queryRqs(String gk, Object gv, QueryRq... rqs) { return new QueriesRq(asList(rqs), singletonMap(gk, gv)); }

    static QueryRq query(String q) { return new QueryRq(q, null); }

    static QueryRq query(String q, String k, Object v) { return new QueryRq(q, singletonMap(k, v)); }

    String json(QueriesRq rq) throws IOException { return tester.write(rq).getJson(); }

    static String str(String name, Object... args) { return Resources.asString(name, lookup(), args); }
}