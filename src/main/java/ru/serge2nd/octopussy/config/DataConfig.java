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
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static java.util.Collections.unmodifiableMap;
import static ru.serge2nd.octopussy.config.CommonConfig.DATA_ENV_DB;
import static ru.serge2nd.octopussy.config.CommonConfig.DATA_ENV_ID;

@Configuration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
public class DataConfig implements DataSourceProvider {
    @Bean @ConfigurationProperties("spring.datasource.hikari")
    Properties baseDataSourceProps() { return new Properties(); }
    @Bean @ConfigurationProperties("spring.jpa.properties")
    Properties baseJpaProps() { return new Properties(); }
    @Component @ConfigurationProperties("octopussy.data.env.arg.mapping")
    private static class DataSourcePropertyNames extends HashMap<String, String> {}

    private final Map<String, String> dataSourcePropertyNames;

    public DataConfig(DataSourcePropertyNames dataSourcePropertyNames) {
        this.dataSourcePropertyNames = unmodifiableMap(dataSourcePropertyNames);
    }

    @Override
    public DataSource getDataSource(Properties dataSourceProps) {
        return new HikariDataSource(new HikariConfig(
                new Properties(baseDataSourceProps()){{putAll(dataSourceProps);}}));
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
        emf.setJpaProperties(
                new Properties(baseJpaProps()){{putAll(jpaProps);}});

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Override
    public PlatformTransactionManager getTransactionManager(EntityManagerFactory emf) {
        HibernateTransactionManager tm = new HibernateTransactionManager();
        tm.setSessionFactory(emf.unwrap(SessionFactory.class));
        return tm;
    }

    @Override
    public Map<String, String> getPropertyNames() {
        return dataSourcePropertyNames;
    }
}
