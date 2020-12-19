package ru.serge2nd.octopussy.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.web.servlet.MockMvc;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.support.DataKitDefinition;
import ru.serge2nd.octopussy.util.QueryWithParams;

import java.util.List;
import java.util.Map;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.api.QueriesRqTest.I;
import static ru.serge2nd.octopussy.api.QueriesRqTest.Q;
import static ru.serge2nd.octopussy.api.QueriesRqTest.V;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitExists;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitNotFound;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataKitDefinitionTest.ID;
import static ru.serge2nd.octopussy.util.Queries.queries;

@SpringBootTest(classes = TestWebConfig.class)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
@TestInstance(Lifecycle.PER_CLASS)
abstract class ContractVerifierBaseTest implements BaseContextTest {
    static final String ID2 = "db3000";
    static final DataKitDefinition DEF2 = new DataKitDefinition(ID2, singletonMap("abc", "xyz"));

    static final Map<String, Object> PARAM = properties(I.toString(), V, format("%f", V), I).toMap();
    static final List<Integer> RS = asList(5, 7);

    @Autowired MockMvc mockMvc;
    @Autowired DataKitService serviceMock;
    @Autowired NativeQueryAdapterProvider providerMock;

    @BeforeAll @SuppressWarnings("unchecked,rawtypes")
    void beforeAll() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        when(serviceMock.getAll())        .thenReturn(asList(DEF, DEF2));
        when(serviceMock.get(eq(ID)))     .thenReturn(DEF);
        when(serviceMock.get(eq(ID2)))    .thenThrow(errDataKitNotFound(ID2));
        when(serviceMock.create(eq(DEF))) .thenReturn(DEF2);
        when(serviceMock.create(eq(DEF2))).thenThrow(errDataKitExists(ID2));
        doThrow(errDataKitNotFound(ID2)).when(serviceMock).delete(eq(ID2));

        when(providerMock.getQueryAdapter(ID).execute(Q, PARAM))
                .thenReturn((List)RS);
        when(providerMock.getQueryAdapter(ID).executeUpdate(queries(new QueryWithParams(Q, PARAM))))
                .thenReturn(RS.stream().mapToInt(i -> i).toArray());
    }
}
