package ru.serge2nd.octopussy.api;

import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.ContextHierarchy;
import org.springframework.test.web.servlet.MockMvc;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.MockServiceLayer;
import ru.serge2nd.octopussy.SpringBootSoftTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.octopussy.spi.DataKitService;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.support.DataKitDefinition;
import ru.serge2nd.octopussy.util.QueryWithParams;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.mockito.Answers.RETURNS_DEEP_STUBS;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static ru.serge2nd.collection.HardProperties.properties;
import static ru.serge2nd.octopussy.AppContractsTesting.*;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitExists;
import static ru.serge2nd.octopussy.service.ex.DataKitException.errDataKitNotFound;
import static ru.serge2nd.octopussy.util.Queries.queries;
import static ru.serge2nd.stream.ArrayCollectors.mapToInts;
import static ru.serge2nd.stream.util.Collecting.collect;

@SpringBootSoftTest
@ContextHierarchy({
    @ContextConfiguration(classes = TestWebConfig.class)
})
@MockServiceLayer
@TestInstance(Lifecycle.PER_CLASS)
abstract class CdcBaseTest implements BaseContextTest {

    static final DataKitDefinition DEF = new DataKitDefinition(ID, properties(
        "url"  , "jdbc:h2:mem:db1000",
        "login", "serge"
    ).toMap());
    static final DataKitDefinition DEF2 = new DataKitDefinition(ID2, singletonMap("abc", "xyz"));

    @Autowired MockMvc mockMvc;
    @Autowired DataKitService serviceMock;
    @Autowired NativeQueryAdapterProvider providerMock;

    @BeforeAll @SuppressWarnings("unchecked")
    void beforeAll() {
        RestAssuredMockMvc.mockMvc(mockMvc);

        when(serviceMock.getAll()).thenReturn(asList(DEF, DEF2));
        when(serviceMock.get(eq(ID))).thenReturn(DEF);
        when(serviceMock.get(eq(ID2))).thenThrow(errDataKitNotFound(ID2));

        when(serviceMock.create(eq(DEF))).thenReturn(DEF2);
        when(serviceMock.create(eq(DEF2))).thenThrow(errDataKitExists(ID2));

        doThrow(errDataKitNotFound(ID2)).when(serviceMock).delete(eq(ID2));

        when(providerMock.getQueryAdapter(ID).execute(Q, PARAM))
                .thenReturn(RS);
        when(providerMock.getQueryAdapter(ID).executeUpdate(queries(new QueryWithParams(Q, PARAM))))
                .thenReturn(collect(RS, mapToInts(x -> (int)x, RS.size())));
    }
}
