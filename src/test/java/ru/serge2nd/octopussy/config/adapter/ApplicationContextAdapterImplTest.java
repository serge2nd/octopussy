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
import org.springframework.test.context.ActiveProfiles;
import ru.serge2nd.octopussy.config.WebConfig;

import java.util.Collection;
import java.util.function.Supplier;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
import static org.springframework.context.annotation.FilterType.ASSIGNABLE_TYPE;

@SpringBootTest(
        classes = ApplicationContextAdapterImplTest.Config.class,
        webEnvironment = NONE)
@ActiveProfiles("test")
class ApplicationContextAdapterImplTest {
    static final String BEAN_NAME1 = "runnable1";
    static final String BEAN_NAME2 = "runnable2";
    static final String METHOD_NAME = "run";

    final ApplicationContextAdapterImpl adapter;
    final GenericApplicationContext ctx;

    @Autowired
    ApplicationContextAdapterImplTest(
            GenericApplicationContext ctx,
            ApplicationContextAdapterFactory factory) {
        this.ctx = ctx;
        this.adapter = (ApplicationContextAdapterImpl) factory.apply(ctx);
    }

    @Mock Supplier<Runnable> supplierMock;
    @Mock Runnable beanMock;
    final Class<Runnable> beanClass = Runnable.class;

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
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(supplierMock)
                .build());

        // THEN
        BeanDefinition bd = ctx.getBeanDefinition(BEAN_NAME1);
        assertTrue(bd.isSingleton(), "expected singleton");
        assertEquals(beanClass.getName(), bd.getBeanClassName(), "class should match");
        verifyNoInteractions(supplierMock);
    }

    @Test
    void testRemoveBean() {
        // GIVEN
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(() -> beanMock)
                .destroyMethod(METHOD_NAME)
                .build());
        ctx.getBean(BEAN_NAME1);

        // WHEN
        adapter.removeBean(BEAN_NAME1);

        // THEN
        assertFalse(ctx.containsBeanDefinition(BEAN_NAME1));
        verify(beanMock, times(1)).run();
    }

    @Test
    void testRemoveBeanNoInit() {
        // GIVEN
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(supplierMock)
                .build());

        // WHEN
        adapter.removeBean(BEAN_NAME1);

        // THEN
        assertFalse(ctx.containsBeanDefinition(BEAN_NAME1));
        verifyNoInteractions(supplierMock);
    }

    @Test
    void testGetBean() {
        // GIVEN
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(() -> beanMock)
                .initMethod(METHOD_NAME)
                .build());

        // WHEN
        Object bean = adapter.getBean(BEAN_NAME1, beanClass);

        // THEN
        assertSame(beanMock, bean, "expected same instance");
        verify(beanMock, times(1)).run();
    }

    @Test
    void testGetBeanSupplyingFailed() {
        // GIVEN
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(() -> {throw new RuntimeException();})
                .build());

        // EXPECT
        assertThrows(
                BeansException.class,
                // WHEN
                () -> adapter.getBean(BEAN_NAME1, beanClass),
                format("expected %s on instantiation error", BeansException.class.getSimpleName()));
    }

    @Test
    void testGetAllBeans() {
        // GIVEN
        Runnable beanMock2 = mock(beanClass);
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(() -> beanMock)
                .build());
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME2)
                .supplier(() -> beanMock2)
                .build());

        // WHEN
        Collection<Runnable> beans = adapter.getBeans(beanClass);

        // THEN
        assertEquals(2, beans.size(), "expected two beans");
        assertTrue(beans.stream().anyMatch(elem -> beanMock == elem), "expected first bean is presented");
        assertTrue(beans.stream().anyMatch(elem -> beanMock2 == elem), "expected second bean is presented");
    }

    @Test
    void testGetAllBeansSupplyingFailed() {
        // GIVEN
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME1)
                .supplier(() -> beanMock)
                .build());
        adapter.addBean(BeanCfg.of(beanClass)
                .name(BEAN_NAME2)
                .supplier(() -> {throw new RuntimeException();})
                .build());

        // EXPECT
        assertThrows(
                BeansException.class,
                // WHEN
                () -> adapter.getBeans(beanClass),
                format("expected %s on instantiation error", BeansException.class.getSimpleName()));
    }

    @Configuration
    @EnableAutoConfiguration
    @ComponentScan(value =
            "ru.serge2nd.octopussy.config",
            excludeFilters = @Filter(type = ASSIGNABLE_TYPE, value = WebConfig.class))
    static class Config {}
}