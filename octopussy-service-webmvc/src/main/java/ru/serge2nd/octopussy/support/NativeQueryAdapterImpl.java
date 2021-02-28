package ru.serge2nd.octopussy.support;

import lombok.RequiredArgsConstructor;
import org.hibernate.query.Query;
import org.hibernate.transform.ResultTransformer;
import org.springframework.transaction.annotation.Transactional;
import ru.serge2nd.octopussy.spi.NativeQueryAdapter;
import ru.serge2nd.octopussy.util.Queries;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static javax.persistence.SynchronizationType.SYNCHRONIZED;
import static org.springframework.transaction.annotation.Propagation.SUPPORTS;
import static ru.serge2nd.stream.ArrayCollectors.mapToInts;
import static ru.serge2nd.stream.util.Collecting.collect;

@RequiredArgsConstructor
public class NativeQueryAdapterImpl implements NativeQueryAdapter {
    private final EntityManagerFactory emf;
    private final ResultTransformer transformer;

    @Transactional(propagation = SUPPORTS, readOnly = true)
    public List<?> execute(String query, Map<String, Object> params) {
        return execute(em -> nativeQuery(query, params, em).getResultList());
    }

    @Transactional
    public int[] executeUpdate(Queries queries) {
        return execute(em -> collect(queries,
            mapToInts(p -> nativeQuery(p.getQuery(), p.getParams(), em).executeUpdate(), queries.size())));
    }

    private <R> R execute(Function<EntityManager, R> op) {
        EntityManager em = null; try {
            em = emf.createEntityManager(SYNCHRONIZED);
            return op.apply(em);
        } finally {
            if (em != null) em.close();
        }
    }

    @SuppressWarnings("deprecation")
    private Query<?> nativeQuery(String sql, Map<String, Object> params, EntityManager em) {
        Query<?> q = em.createNativeQuery(sql).unwrap(Query.class);
        params.forEach(q::setParameter);
        return q.setResultTransformer(transformer);
    }
}
