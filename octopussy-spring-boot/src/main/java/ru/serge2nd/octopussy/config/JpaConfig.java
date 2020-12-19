package ru.serge2nd.octopussy.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.hibernate.transform.ResultTransformer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import ru.serge2nd.bean.processor.Immutable;
import ru.serge2nd.octopussy.spi.JpaResultTransformer;
import ru.serge2nd.octopussy.support.JpaResultToListTransformer;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.serge2nd.collection.HardProperties.from;
import static ru.serge2nd.octopussy.App.DATA_KIT_ID;
import static ru.serge2nd.octopussy.App.DATA_KIT_PKGS;

@Configuration
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

        emf.setPersistenceUnitName(mergedProps.get(DATA_KIT_ID) + "PU");
        emf.setPackagesToScan(mergedProps.get(DATA_KIT_PKGS));
        emf.setJpaPropertyMap(mergedProps);

        emf.afterPropertiesSet();
        return emf.getObject();
    }

    @Override
    public ResultTransformer getResultTransformer() { return new ResultTransformer() {
        public Object transformTuple(Object[] tuple, String[] aliases) { return DELEGATE.transform(tuple, aliases); }
        public List<?> transformList(List list)                        { return DELEGATE.transform(list); }
    };}

    static final JpaResultTransformer DELEGATE = new JpaResultToListTransformer();
}
