package ru.serge2nd.octopussy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.spi.DataSourceProvider;
import ru.serge2nd.octopussy.spi.DataEnvironment;
import ru.serge2nd.octopussy.spi.NativeQueryAdapterProvider;
import ru.serge2nd.octopussy.service.DataEnvironmentService;
import ru.serge2nd.octopussy.support.DataEnvironmentDefinition;
import ru.serge2nd.octopussy.support.DataEnvironmentImpl;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.support.NativeQueryAdapterProviderImpl;
import ru.serge2nd.util.HardProperties;
import ru.serge2nd.util.bean.Immutable;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ru.serge2nd.octopussy.config.CommonConfig.DATA_ENV_DB;
import static ru.serge2nd.octopussy.config.CommonConfig.DATA_ENV_ID;
import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@Configuration
@EnableCaching
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@RequiredArgsConstructor
public class DataConfig implements DataSourceProvider {
    @Immutable @Bean @ConfigurationProperties("spring.datasource.hikari")
    Properties baseDataSourceProps() { return new Properties(); }
    @Immutable @Bean @ConfigurationProperties("spring.jpa.properties")
    Properties baseJpaProps() { return new Properties(); }
    @Immutable @Bean @ConfigurationProperties("octopussy.data.env.arg.mapping")
    Map<String, String> dataSourcePropertyNames() { return new HashMap<>(); }

    @Primary @Bean
    NativeQueryAdapterProvider nativeQueryAdapterProvider(DataEnvironmentService dataEnvService) {
        return new NativeQueryAdapterProvider() {
            @Cacheable(
                key = "#p0.definition.envId",
                cacheNames = QUERY_ADAPTERS_CACHE)
            public NativeQueryAdapter getQueryAdapter(DataEnvironment raw) {
                return this.getQueryAdapter(raw.getDefinition().getEnvId());
            }
            @Cacheable(QUERY_ADAPTERS_CACHE)
            public NativeQueryAdapter getQueryAdapter(String envId) {
                return dataEnvService.doWith(envId, dataEnv -> nqaProviderTarget().getQueryAdapter(dataEnv));
            }
        };
    }
    @Bean NativeQueryAdapterProvider nqaProviderTarget() { return new NativeQueryAdapterProviderImpl(); }

    @Override
    public DataEnvironment getDataEnvironment(DataEnvironmentDefinition definition) {
        return new DataEnvironmentImpl(definition, this);
    }

    @Override
    public Map<String, String> getPropertyNames() {
        return dataSourcePropertyNames();
    }

    @Override
    public DataSource getDataSource(Properties dataSourceProps) {
        return new HikariDataSource(new HikariConfig(
                HardProperties.from(baseDataSourceProps(), dataSourceProps)));
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Properties jpaProps) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter() {{
            setDatabase(Database.valueOf(jpaProps.getProperty(DATA_ENV_DB)));
        }});

        emf.setPersistenceUnitName(jpaProps.getProperty(DATA_ENV_ID) + "PU");
        emf.setPackagesToScan("ru.serge2nd.octopussy.data");
        emf.setJpaProperties(HardProperties.from(baseJpaProps(), jpaProps));

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager(EntityManagerFactory emf) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setSessionFactory(emf.unwrap(SessionFactory.class));
        return tm;
    }
}
