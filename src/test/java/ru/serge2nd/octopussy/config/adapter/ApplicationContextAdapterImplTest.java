package ru.serge2nd.octopussy.config.adapter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.GenericApplicationContext;
import ru.serge2nd.octopussy.config.WebConfig;

import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootTest(
        classes = ApplicationContextAdapterImplTest.Config.class,
        webEnvironment = NONE)
class ApplicationContextAdapterImplTest {
    private static final String BEAN_NAME1 = "runnable1";
    private static final String BEAN_NAME2 = "runnable2";
    private static final String METHOD_NAME = "run";

    @Autowired
    private ApplicationContextAdapterImpl adapter;
    @Autowired
    private GenericApplicationContext ctx;

    @Mock
    private Supplier<Thread> supplierMock;
    @Mock
    private Thread beanMock;

    @BeforeEach
    void setUp() {
        if (ctx.containsBean(BEAN_NAME1))
            ctx.removeBeanDefinition(BEAN_NAME1);
        if (ctx.containsBean(BEAN_NAME2))
            ctx.removeBeanDefinition(BEAN_NAME2);
    }

    @Test
    void testAddBean() {
        // WHEN
        adapter.addBean(BEAN_NAME1, Thread.class, supplierMock);

        // THEN
        BeanDefinition bd = ctx.getBeanDefinition(BEAN_NAME1);
        assertTrue(bd.isSingleton(), "expected singleton");
        assertEquals(Thread.class.getName(), bd.getBeanClassName(), "class should match");
        verifyZeroInteractions(supplierMock);
    }

    @Test
    void testRemoveBean() {
        // GIVEN
        adapter.addBean(BEAN_NAME1, Thread.class, () -> beanMock, bd -> bd.setDestroyMethodName(METHOD_NAME));
        ctx.getBean(BEAN_NAME1);

        // WHEN
        adapter.removeBean(BEAN_NAME1);

        // THEN
        assertFalse(ctx.containsBean(BEAN_NAME1));
        verify(beanMock, times(1)).run();
    }

    @Test
    void testRemoveBeanNoInit() {
        // GIVEN
        adapter.addBean(BEAN_NAME1, Thread.class, supplierMock);

        // WHEN
        adapter.removeBean(BEAN_NAME1);

        // THEN
        assertFalse(ctx.containsBean(BEAN_NAME1));
        verifyZeroInteractions(supplierMock);
    }

    @Test
    void testGetBean() {
        // GIVEN
        adapter.addBean(BEAN_NAME1, Thread.class, () -> beanMock, bd -> bd.setInitMethodName(METHOD_NAME));

        // WHEN
        Object bean = adapter.getBean(BEAN_NAME1, Thread.class);

        // THEN
        assertSame(beanMock, bean, "expected same instance");
        verify(beanMock, times(1)).run();
    }

    @Test
    void testGetBeanSupplyingFailed() {
        // GIVEN
        adapter.addBean(BEAN_NAME1, Thread.class, () -> {throw new RuntimeException();});

        // EXPECT
        assertThrows(
                BeansException.class,
                // WHEN
                () -> adapter.getBean(BEAN_NAME1, Thread.class),
                format("expected %s on instantiation error", BeansException.class.getSimpleName()));
    }

    @Test
    void testGetAllBeans() {
        // GIVEN
        Thread beanMock2 = mock(Thread.class);
        adapter.addBean(BEAN_NAME1, Thread.class, () -> beanMock);
        adapter.addBean(BEAN_NAME2, Thread.class, () -> beanMock2);

        // WHEN
        Thread[] beans = adapter.getBeans(Thread.class).toArray(new Thread[0]);

        // THEN
        assertEquals(2, beans.length, "expected two beans");
        assertSame(beanMock, beans[0], "expected first bean is presented");
        assertSame(beanMock2, beans[1], "expected second bean is presented");
    }

    @Test
    void testGetAllBeansSupplyingFailed() {
        // GIVEN
        adapter.addBean(BEAN_NAME1, Thread.class, () -> beanMock);
        adapter.addBean(BEAN_NAME2, Thread.class, () -> {throw new RuntimeException();});

        // EXPECT
        assertThrows(
                BeansException.class,
                // WHEN
                () -> adapter.getBeans(Thread.class),
                format("expected %s on instantiation error", BeansException.class.getSimpleName()));
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value =
            "ru.serge2nd.octopussy.config",
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}