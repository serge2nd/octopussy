package ru.serge2nd.octopussy.config.adapter;

import lombok.*;

import java.util.function.Supplier;

@Value
@Builder
public class BeanCfg {
    @NonNull
    String name;
    @NonNull @EqualsAndHashCode.Exclude @ToString.Exclude
    Supplier<?> supplier;

    Class<?> beanClass;

    String initMethod;

    String destroyMethod;

    @Builder.Default
    boolean lazyInit = true;

    @Builder.Default
    String scope = "singleton";

    public static BeanCfg.BeanCfgBuilder of(Class<?> beanClass) {
        return BeanCfg.builder().beanClass(beanClass);
    }
}
