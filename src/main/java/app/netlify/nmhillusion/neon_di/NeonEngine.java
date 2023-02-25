package app.netlify.nmhillusion.neon_di;

import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.inject.Injector;
import app.netlify.nmhillusion.neon_di.pool.Population;
import app.netlify.nmhillusion.neon_di.scanner.DependencyScanner;
import app.netlify.nmhillusion.neon_di.store.PersistentStore;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * date: 2022-02-02
 * <p>
 * created-by: nmhillusion
 */

public class NeonEngine {
    private final PersistentStore persistentStore = new PersistentStore();
    private final DependencyScanner dependencyScanner = new DependencyScanner(persistentStore);
    private final Population population = new Population(persistentStore);
    private final Injector injector = new Injector(persistentStore);

    public void run(Class<?> startClass) throws NeonException {
        try {
            dependencyScanner.scan(startClass);
            population.populate();
            injector.inject();
        } catch (Exception ex) {
            throw new NeonException(ex);
        }
    }

    public <T> List<T> findNeonListByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findNeonInstancesByClass(classToFind);
    }

    public <T> Optional<T> findFirstNeonByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findFirstNeonInstanceByClass(classToFind);
    }

    public NeonEngine putProperties(Map<String, Object> moreProperties) {
        persistentStore.putAllProperties(moreProperties);
        return this;
    }

    public NeonEngine putProperty(String propertyKey, Object propertyValue) {
        persistentStore.putProperty(propertyKey, propertyValue);
        return this;
    }
}
