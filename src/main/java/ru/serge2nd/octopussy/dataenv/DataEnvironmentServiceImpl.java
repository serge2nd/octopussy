package ru.serge2nd.octopussy.dataenv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanNotOfRequiredTypeException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import ru.serge2nd.octopussy.config.spi.DataSourceProvider;
import ru.serge2nd.octopussy.config.adapter.ApplicationContextAdapter;

import java.util.Collection;
import java.util.Optional;

import static ru.serge2nd.octopussy.config.CommonConfig.QUERY_ADAPTERS_CACHE;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataEnvironmentServiceImpl implements DataEnvironmentService {
    private static final String DATA_ENV_SUFFIX = "DataEnvironment";

    private final ApplicationContextAdapter ctx;
    private final DataSourceProvider dataSourceProvider;

    @Override
    public DataEnvironment get(String envId) {
        try {
            return ctx.getBean(dataEnvName(envId), DataEnvironment.class);
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            throw new DataEnvironmentNotFoundException(envId);
        } catch (BeansException e) {
            throw new DataEnvironmentInitException(envId, e);
        }
    }

    @Override
    public Optional<DataEnvironment> find(String envId) {
        try {
            return Optional.of(ctx.getBean(dataEnvName(envId), DataEnvironment.class));
        } catch (NoSuchBeanDefinitionException | BeanNotOfRequiredTypeException e) {
            return Optional.empty();
        } catch (BeansException e) {
            throw new DataEnvironmentInitException(envId, e);
        }
    }

    @Override
    public Collection<DataEnvironment> getAll() {
        try {
            return ctx.getBeans(DataEnvironment.class);
        } catch (BeansException e) {
            throw new DataEnvironmentInitException("", e);
        }
    }

    @Override
    public DataEnvironment create(DataEnvironment toCreate) throws BeansException {
        DataEnvironmentDefinition definition = toCreate.getDefinition();
        String envId = definition.getEnvId();

        try {
            ctx.addBean(
                    dataEnvName(envId),
                    DataEnvironment.class,
                    () -> new DataEnvironment(definition, dataSourceProvider),
                    bd -> bd.setDestroyMethodName("close"));
        } catch (BeanDefinitionStoreException e) {
            throw new DataEnvironmentExistsException(envId);
        }

        return toCreate;
    }

    @Override
    @CacheEvict(QUERY_ADAPTERS_CACHE)
    public void delete(String envId) {
        try {
            ctx.removeBean(dataEnvName(envId));
        } catch (NoSuchBeanDefinitionException e) {
            throw new DataEnvironmentNotFoundException(envId);
        }
    }

    private static String dataEnvName(String envId) {
        return envId + DATA_ENV_SUFFIX;
    }
}
