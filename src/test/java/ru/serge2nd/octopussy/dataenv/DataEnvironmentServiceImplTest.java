package ru.serge2nd.octopussy.dataenv;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinitionCustomizer;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapter;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DataEnvironmentServiceImplTest {
    private static final String ID1 = "5010";
    private static final String ID2 = "7010";
    private static final String NAME1 = ID1 + "DataEnvironment";
    private static final String NAME2 = ID2 + "DataEnvironment";

    @InjectMocks
    private DataEnvironmentServiceImpl dataEnvService;
    @Mock
    private ApplicationContextAdapter ctxMock;

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

        // EXPECT
        assertThrows(
                DataEnvironmentNotFoundException.class,
                // WHEN
                () -> dataEnvService.get(ID1),
                "expected not found");
        // EXPECT
        assertThrows(
                DataEnvironmentNotFoundException.class,
                // WHEN
                () -> dataEnvService.get(ID2),
                "expected not found");
    }

    @Test
    void testGetInitFailed() {
        // GIVEN
        when(ctxMock.getBean(NAME1, DataEnvironment.class)).thenThrow(new BeansException("") {});

        // EXPECT
        assertThrows(
                DataEnvironmentInitException.class,
                // WHEN
                () -> dataEnvService.get(ID1),
                "expected failed initialization");
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

        // EXPECT
        assertThrows(
                DataEnvironmentInitException.class,
                // WHEN
                () -> dataEnvService.find(ID1),
                "expected failed initialization");
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

        // EXPECT
        assertThrows(
                DataEnvironmentInitException.class,
                // WHEN
                () -> dataEnvService.getAll(),
                "expected failed initialization");
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
        ArgumentCaptor<String> name = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Class<DataEnvironment>> clazz = ArgumentCaptor.forClass(Class.class);
        ArgumentCaptor<Supplier<DataEnvironment>> instance = ArgumentCaptor.forClass(Supplier.class);
        ArgumentCaptor<BeanDefinitionCustomizer> customizers = ArgumentCaptor.forClass(BeanDefinitionCustomizer.class);
        verify(ctxMock, times(1)).addBean(name.capture(), clazz.capture(), instance.capture(), customizers.capture());
        // AND
        assertEquals(NAME1, name.getValue(), "name should match");
        assertSame(DataEnvironment.class, clazz.getValue(), "class should match");
        assertNotNull(instance.getValue(), "instance should be presented");
        assertEquals(1, customizers.getAllValues().size(), "expected one customizer");
        assertNotNull(customizers.getAllValues().get(0), "customizer should be presented");
    }

    @Test
    void testCreateAlreadyExists() {
        // GIVEN
        doThrow(BeanDefinitionStoreException.class).when(ctxMock).addBean(eq(NAME1), same(DataEnvironment.class), any(), any());

        // EXPECT
        assertThrows(
                DataEnvironmentExistsException.class,
                // WHEN
                () -> dataEnvService.create(dataEnv(ID1)),
                "expected already exists");
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

        // EXPECT
        assertThrows(
                DataEnvironmentNotFoundException.class,
                // WHEN
                () -> dataEnvService.delete(ID1),
                "expected not found");
    }

    private static DataEnvironment dataEnv(String envId) {
        return DataEnvironment.builder()
                .definition(DataEnvironmentDefinition.builder()
                        .envId(envId)
                        .build())
                .build();
    }
}
