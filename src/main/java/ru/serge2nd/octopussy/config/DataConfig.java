package ru.serge2nd.octopussy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import org.hibernate.SessionFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import ru.serge2nd.octopussy.config.properties.DataSourceProperties;
import ru.serge2nd.octopussy.config.properties.JpaProperties;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@EnableAutoConfiguration(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class})
@RequiredArgsConstructor
public class DataConfig implements DataSourceProvider {
    private static final String PROP_ENV_ID = "octopussy.data.env.id";
    private static final String PROP_DB = "octopussy.db";

    private final DataSourceProperties baseDataSourceProps;
    private final JpaProperties baseJpaProps;

    @Override
    public String envIdPropertyName() { return PROP_ENV_ID; }
    @Override
    public String dbPropertyName() { return PROP_DB; }
    @Override
    public String driverClassPropertyName() { return "driverClassName"; }
    @Override
    public String urlPropertyName() { return "jdbcUrl"; }
    @Override
    public String loginPropertyName() { return "username"; }
    @Override
    public String passwordPropertyName() { return "password"; }

    @Override
    public DataSource getDataSource(Properties dataSourceProps) {
        return new HikariDataSource(new HikariConfig(
                new Properties(baseDataSourceProps){{putAll(dataSourceProps);}}));
    }

    @Override
    public EntityManagerFactory getEntityManagerFactory(DataSource dataSource, Properties jpaProps) {
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();

        emf.setDataSource(dataSource);
        emf.setJpaVendorAdapter(new HibernateJpaVendorAdapter() {{
            setDatabase(Database.valueOf(jpaProps.getProperty(PROP_DB)));
        }});

        emf.setPersistenceUnitName(jpaProps.getProperty(PROP_ENV_ID) + "PU");
        emf.setPackagesToScan("ru.serge2nd.octopussy.data");
        emf.setJpaProperties(
                new Properties(baseJpaProps){{putAll(jpaProps);}});

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
