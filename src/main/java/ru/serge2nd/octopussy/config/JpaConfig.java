package ru.serge2nd.octopussy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.bean.processor.Immutable;
import ru.serge2nd.octopussy.spi.PersistenceUnitProvider;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static ru.serge2nd.collection.HardProperties.from;
import static ru.serge2nd.octopussy.App.DATA_ENV_ID;
import static ru.serge2nd.octopussy.App.DATA_ENV_PKGS;

@Configuration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class JpaConfig implements PersistenceUnitProvider {
    @Immutable@Bean @ConfigurationProperties("spring.datasource.hikari")
    Map<String, String> baseDataSourceProps() { return new HashMap<>(); }
    @Immutable@Bean @ConfigurationProperties("spring.jpa.properties")
    Map<String, String> baseJpaProps()        { return new HashMap<>(); }

    @Override
    public DataSource getDataSource(Map<String, Object> dataSourceProps) {
        return new HikariDataSource(new HikariConfig(from(baseDataSourceProps(), dataSourceProps)));
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Map<String, Object> jpaProps) {
        Map<String, String> mergedProps = from(baseJpaProps(), jpaProps).toMap();
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

        emf.setPersistenceUnitName(mergedProps.get(DATA_ENV_ID) + "PU");
        emf.setPackagesToScan(mergedProps.get(DATA_ENV_PKGS));
        emf.setJpaPropertyMap(mergedProps);

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager(EntityManagerFactory emf) {
        return new HibernateTransactionManager(emf.unwrap(SessionFactory.class));
    }
}
