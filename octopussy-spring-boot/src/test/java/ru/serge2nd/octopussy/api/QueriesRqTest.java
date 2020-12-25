package ru.serge2nd.octopussy.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.test.util.Resources;

import javax.validation.Validator;
import java.io.IOException;
import java.io.StringReader;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static ru.serge2nd.test.match.AssertThat.assertThat;
import static ru.serge2nd.test.util.CustomMatchers.equalToJson;

@TestInstance(Lifecycle.PER_CLASS)
@JsonTest @Import(LocalValidatorFactoryBean.class)
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
class QueriesRqTest implements BaseContextTest {
    static final String J = "query_rq_tmpl.json";
    static final String Q = "select smth from smwh";
    static final Integer I = Integer.MAX_VALUE;
    static final String K = I.toString();
    static final Double V = 3.14159;
    static final QueryRq RQ = query(Q, K, V);
    static final QueriesRq RQS = queryRqs(format("%f", V), I, RQ);

    @Autowired JacksonTester<QueriesRq> tester;
    @Autowired Validator validator;

    static Stream<Arguments> validQueryRqProvider() { return Stream.of(
            arguments("query", queries(Q)),
            arguments("param", RQS),
            arguments("global param", queryRqs(K, V, query(Q))),
            arguments("null param", queryRqs(K, V, query(Q, K, null))),
            arguments("null global param", queryRqs(K, null, query(Q))),
            arguments("two queries", queryRqs(RQ, RQ))); }
    static Stream<Arguments> invalidQueryRqProvider() { return Stream.of(
            arguments("null queries", new QueriesRq(null, emptyMap())),
            arguments("no queries", queries()),
            arguments("null query", queries((String)null)),
            arguments("null query rq", queryRqs((QueryRq)null)),
            arguments("empty query", queryRqs(query(" \t\n"))),
            arguments("null param key", queryRqs(query(Q, null, V))),
            arguments("null global param key", queryRqs(null, V, query(Q))),
            arguments("empty param key", queryRqs(query(Q, " \t\n", V))),
            arguments("empty global param key", queryRqs(" \t\n", V, query(Q)))); }

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
        QueriesRq result = tester.read(new StringReader(str(J, Q, I, V, format("%f", V), I))).getObject();

        // THEN
        assertEquals(RQS, result, "read mismatches");
    }

    @Test
    void testWrite() throws IOException {
        assertThat(json(RQS), equalToJson(str(J, Q, I, V, format("%f", V), I)));
    }

    static QueriesRq queries(String... qs) { return new QueriesRq(stream(qs).map(QueriesRqTest::query).collect(toList()), null); }

    static QueriesRq queries(String gk, Object gv, String... qs) { return new QueriesRq(stream(qs).map(QueriesRqTest::query).collect(toList()), singletonMap(gk, gv)); }

    static QueriesRq queryRqs(QueryRq... rqs) { return new QueriesRq(asList(rqs), null); }

    static QueriesRq queryRqs(String gk, Object gv, QueryRq... rqs) { return new QueriesRq(asList(rqs), singletonMap(gk, gv)); }

    static QueryRq query(String q) { return new QueryRq(q, null); }

    static QueryRq query(String q, String k, Object v) { return new QueryRq(q, singletonMap(k, v)); }

    String json(QueriesRq rq) throws IOException { return tester.write(rq).getJson(); }

    static String str(String name, Object... args) { return Resources.asString(name, lookup(), args); }
}