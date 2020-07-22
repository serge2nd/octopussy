package ru.serge2nd.octopussy.dataenv;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapter;
import ru.serge2nd.octopussy.config.adapter.BeanCfg;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DataEnvironmentServiceImplTest {
    static final String ID1 = "5010";
    static final String ID2 = "7010";
    static final String NAME1 = ID1 + "DataEnvironment";
    static final String NAME2 = ID2 + "DataEnvironment";

    @Mock ApplicationContextAdapter ctxMock;
    DataEnvironmentServiceImpl dataEnvService;

    @BeforeEach
    void setUp() {
        dataEnvService = new DataEnvironmentServiceImpl(
                null, mock(DataSourceProvider.class), $ -> ctxMock);
    }

    @Test
    void testGet() {
        // GIVEN
        DataEnvironment dataEnv = dataEnv(ID1);
        when(ctxMock.getBean(NAME1, DataEnvironment.class)).thenReturn(dataEnv);

        // WHEN
        DataEnvironment result = dataEnvService.get(ID1);

        // THEN
        assertSame(dataEnv, result, "expected one from context");
    }

    @Test
    void testGetNotFound() {
        // GIVEN
        doThrow(NoSuchBeanDefinitionException.class).when(ctxMock).getBean(NAME1, DataEnvironment.class);
        doThrow(BeanNotOfRequiredTypeException.class).when(ctxMock).getBean(NAME2, DataEnvironment.class);

        // WHEN
        Throwable thrown1 = catchThrowable(() -> dataEnvService.get(ID1));
        Throwable thrown2 = catchThrowable(() -> dataEnvService.get(ID2));

        // THEN
        assertTrue(thrown1 instanceof DataEnvironmentNotFoundException, "expected error due to absence");
        assertTrue(thrown2 instanceof DataEnvironmentNotFoundException, "expected error due to absence");
    }

    @Test
    void testGetInitFailed() {
        // GIVEN
        when(ctxMock.getBean(NAME1, DataEnvironment.class)).thenThrow(new BeansException("") {});

        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.get(ID1));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentInitException, "expected failed initialization");
    }

    @Test
    void testFind() {
        // GIVEN
        DataEnvironment dataEnv = dataEnv(ID1);
        when(ctxMock.getBean(NAME1, DataEnvironment.class)).thenReturn(dataEnv);

        // WHEN
        Optional<DataEnvironment> result = dataEnvService.find(ID1);

        // THEN
        assertTrue(result.isPresent(), "should be presented");
        assertSame(dataEnv, result.get(), "expected one from context");
    }

    @Test
    void testFindNotFound() {
        // GIVEN
        doThrow(NoSuchBeanDefinitionException.class).when(ctxMock).getBean(NAME1, DataEnvironment.class);
        doThrow(BeanNotOfRequiredTypeException.class).when(ctxMock).getBean(NAME2, DataEnvironment.class);

        // WHEN
        Optional<DataEnvironment> result1 = dataEnvService.find(ID1);
        Optional<DataEnvironment> result2 = dataEnvService.find(ID2);

        // THEN
        assertFalse(result1.isPresent() || result2.isPresent(), "should be empty if not found");
    }

    @Test
    void testFindInitFailed() {
        // GIVEN
        when(ctxMock.getBean(NAME1, DataEnvironment.class)).thenThrow(new BeansException("") {});

        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.get(ID1));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentInitException, "expected failed initialization");
    }

    @Test
    void testGetAll() {
        // GIVEN
        when(ctxMock.getBeans(DataEnvironment.class)).thenReturn(emptyList());

        // WHEN
        Collection<DataEnvironment> result = dataEnvService.getAll();

        // THEN
        assertSame(emptyList(), result, "expected collection from context");
    }

    @Test
    void testGetAllInitFailed() {
        // GIVEN
        when(ctxMock.getBeans(DataEnvironment.class)).thenThrow(new BeansException("") {});

        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.getAll());

        // THEN
        assertTrue(thrown instanceof DataEnvironmentInitException, "expected failed initialization");
    }

    @Test
    void testCreate() {
        // GIVEN
        DataEnvironment toCreate = dataEnv(ID1);

        // WHEN
        DataEnvironment result = dataEnvService.create(toCreate);

        // THEN
        assertSame(toCreate, result, "expected same as passed");
        // AND
        ArgumentCaptor<BeanCfg> beanCfg = ArgumentCaptor.forClass(BeanCfg.class);
        verify(ctxMock, times(1)).addBean(beanCfg.capture());
        String name = beanCfg.getValue().getName();
        Class<?> clazz = beanCfg.getValue().getBeanClass();
        Supplier<?> supplier = beanCfg.getValue().getSupplier();
        String destroyMethod = beanCfg.getValue().getDestroyMethod();
        // AND
        assertEquals(NAME1, name, "name should match");
        assertSame(DataEnvironment.class, clazz, "class should match");
        assertNotNull(supplier, "instance should be presented");
        assertEquals("close", destroyMethod, "expected real destroy method");
    }

    @Test
    void testCreateAlreadyExists() {
        // GIVEN
        doThrow(BeanDefinitionStoreException.class).when(ctxMock)
                .addBean(eq(BeanCfg.of(DataEnvironment.class)
                        .name(NAME1)
                        .supplier(String::new)
                        .destroyMethod("close")
                        .build()));

        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.create(dataEnv(ID1)));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentExistsException, "expected already exists");
    }

    @Test
    void testDelete() {
        // WHEN
        dataEnvService.delete(ID1);

        // THEN
        verify(ctxMock, times(1)).removeBean(NAME1);
    }

    @Test
    void testDeleteNotFound() {
        // GIVEN
        doThrow(NoSuchBeanDefinitionException.class).when(ctxMock).removeBean(NAME1);

        // WHEN
        Throwable thrown = catchThrowable(() -> dataEnvService.delete(ID1));

        // THEN
        assertTrue(thrown instanceof DataEnvironmentNotFoundException, "expected not found");
    }

    static DataEnvironment dataEnv(String envId) {
        return DataEnvironment.builder()
                .definition(DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .build())
                .build();
    }
}
