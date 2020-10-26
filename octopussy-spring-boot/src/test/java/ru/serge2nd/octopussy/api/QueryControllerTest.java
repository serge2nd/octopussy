package ru.serge2nd.octopussy.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.util.QueryWithParams;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.api.DataEnvironmentControllerTest.str;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;
import static ru.serge2nd.octopussy.util.Queries.queries;
import static ru.serge2nd.test.matcher.CommonMatch.equalTo;

@SpringBootTest(classes = TestWebConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
class QueryControllerTest implements BaseContextTest {
    static final String J = "query_rq_tmpl.json";
    static final String Q = "not executed";
    static final Integer K = Integer.MAX_VALUE;
    static final Double V = 0.5;
    static final Map<String, Object> PARAM = properties(K.toString(), V, format("%f", V), K).toMap();
    static final List<Integer> RS = asList(5, 7);
    
    static final String QUERY_URL = format("/dataEnvironments/%s/query", ID);
    static final String UPDATE_URL = format("/dataEnvironments/%s/update", ID);

    @Autowired MockMvc mockMvc;
    @MockBean NativeQueryAdapterProvider providerMock;

    @Test @SuppressWarnings("unchecked,rawtypes")
    void testQuery() throws Exception {
        // GIVEN
        when(providerMock.getQueryAdapter(ID).execute(Q, PARAM)).thenReturn((List)RS);

        // WHEN
        mockMvc.perform(post(QUERY_URL).content(str(J, Q, K, V, format("%f", V), K)))

        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", equalTo(RS)));
    }

    @Test
    void testInvalidQuery() throws Exception {
        // WHEN
        mockMvc.perform(post(QUERY_URL).content("{}"))

        // THEN
        .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdate() throws Exception {
        // GIVEN
        when(providerMock.getQueryAdapter(ID).executeUpdate(queries(new QueryWithParams(Q, PARAM))))
                .thenReturn(RS.stream().mapToInt(i -> i).toArray());

        // WHEN
        mockMvc.perform(post(UPDATE_URL).content(str(J, Q, K, V, format("%f", V), K)))

        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", equalTo(RS)));
    }

    @Test
    void testInvalidUpdate() throws Exception {
        // WHEN
        mockMvc.perform(post(UPDATE_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))

        // THEN
        .andExpect(status().isBadRequest());
    }
}