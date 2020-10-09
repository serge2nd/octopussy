package ru.serge2nd.octopussy.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.serge2nd.octopussy.BaseContextTest;
import ru.serge2nd.octopussy.TestWebConfig;
import ru.serge2nd.octopussy.service.ex.DataEnvironmentException;
import ru.serge2nd.octopussy.spi.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.test.util.Resources;

import static java.lang.String.format;
import static java.lang.invoke.MethodHandles.lookup;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.CoreMatchers.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvExists;
import static ru.serge2nd.octopussy.service.ex.DataEnvironmentException.errDataEnvNotFound;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.DEF;
import static ru.serge2nd.octopussy.support.DataEnvironmentDefinitionTest.ID;

@SpringBootTest(classes = TestWebConfig.class)
@TestInstance(Lifecycle.PER_CLASS)
public class DataEnvironmentControllerTest implements BaseContextTest {
    static final String ID2 = "db3000";
    static final DataEnvironmentDefinition DEF2 = new DataEnvironmentDefinition(ID2, singletonMap("abc", "xyz"));

    static final String ONE_URL = format("/dataEnvironments/%s", ID);
    static final String ALL_URL = "/dataEnvironments";

    @Autowired MockMvc mockMvc;
    @MockBean DataEnvironmentService serviceMock;

    @Test void testGetOne() throws Exception {
        // GIVEN
        when(serviceMock.get(ID)).thenReturn(DEF);

        // WHEN
        mockMvc.perform(get(ONE_URL))

        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.envId", is(ID)))
        .andExpect(jsonPath("$.properties", is(DEF.getProperties())));
    }

    @Test void testGetOneNotFound() throws Exception {
        // GIVEN
        DataEnvironmentException e = errDataEnvNotFound(ID2);
        when(serviceMock.get(ID)).thenThrow(e);

        // WHEN
        mockMvc.perform(get(ONE_URL))

        // THEN
        .andExpect(status().isNotFound())
        .andExpect(jsonPath("$.url", endsWith(ONE_URL)))
        .andExpect(jsonPath("$.status", startsWith(""+NOT_FOUND.value())))
        .andExpect(jsonPath("$.code", is("DATA_ENV_NOT_FOUND")))
        .andExpect(jsonPath("$.message", is(e.getMessage())));
    }

    @Test void testGetAll() throws Exception {
        // GIVEN
        when(serviceMock.getAll()).thenReturn(asList(DEF, DEF2));

        // WHEN
        mockMvc.perform(get(ALL_URL))

        // THEN
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.[0].envId", is(ID)))
        .andExpect(jsonPath("$.[0].properties", is(DEF.getProperties())))
        .andExpect(jsonPath("$.[1].envId", is(ID2)))
        .andExpect(jsonPath("$.[1].properties", is(DEF2.getProperties())));
    }

    @Test void testCreate() throws Exception {
        // GIVEN
        when(serviceMock.create(DEF)).thenReturn(DEF2);

        // WHEN
        mockMvc.perform(post(ALL_URL).content(str("../support/data_env.json")))

        // THEN
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.envId", is(ID2)))
        .andExpect(jsonPath("$.properties", is(DEF2.getProperties())));
    }

    @Test void testCreateExists() throws Exception {
        // GIVEN
        DataEnvironmentException e = errDataEnvExists(ID2);
        when(serviceMock.create(DEF)).thenThrow(e);

        // WHEN
        mockMvc.perform(post(ALL_URL).content(str("../support/data_env.json")))

        // THEN
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.url", endsWith(ALL_URL)))
        .andExpect(jsonPath("$.status", startsWith(""+BAD_REQUEST.value())))
        .andExpect(jsonPath("$.code", is("DATA_ENV_EXISTS")))
        .andExpect(jsonPath("$.message", is(e.getMessage())));
    }

    @Test void testDelete() throws Exception {
        // WHEN
        mockMvc.perform(delete(ONE_URL))

        // THEN
        .andExpect(status().isNoContent())
        .andExpect($->verify(serviceMock, times(1)).delete(ID));
    }

    static String str(String name, Object... args) {
        return Resources.asString(name, lookup().lookupClass(), args);
    }
}
