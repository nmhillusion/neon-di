package tech.nmhillusion.neon_di;

import tech.nmhillusion.neon_di.exception.NeonException;
import tech.nmhillusion.neon_di.inject.Injector;
import tech.nmhillusion.neon_di.pool.Population;
import tech.nmhillusion.neon_di.scanner.DependencyScanner;
import tech.nmhillusion.neon_di.store.PersistentStore;

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
            ex.printStackTrace();
            throw new NeonException(ex);
        }
    }

    public <T> List<T> findNeonListByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findNeonInstancesByClass(classToFind);
    }

    public <T> Optional<T> findFirstNeonByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findFirstNeonInstanceByClass(classToFind);
    }

    public <T> T makeSureObtainNeon(Class<T> clazz2Obtain) {
        final Optional<T> neonByClass = findFirstNeonByClass(clazz2Obtain);
        if (neonByClass.isEmpty()) {
            throw new RuntimeException("Cannot find instance of " + clazz2Obtain);
        }

        return neonByClass.get();
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
