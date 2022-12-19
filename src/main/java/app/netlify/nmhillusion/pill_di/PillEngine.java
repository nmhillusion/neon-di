package app.netlify.nmhillusion.pill_di;

import app.netlify.nmhillusion.pill_di.inject.Injector;
import app.netlify.nmhillusion.pill_di.pool.Population;
import app.netlify.nmhillusion.pill_di.scanner.DependencyScanner;
import app.netlify.nmhillusion.pill_di.store.PersistentStore;

import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class PillEngine {
    private final PersistentStore persistentStore = new PersistentStore();
    private final DependencyScanner dependencyScanner = new DependencyScanner(persistentStore);
    private final Population population = new Population(persistentStore);
    private final Injector injector = new Injector(persistentStore);

    public void run(Class<?> startClass) throws MalformedURLException, URISyntaxException, InvocationTargetException, InstantiationException, IllegalAccessException {
        dependencyScanner.scan(startClass);
        population.populate();
        injector.inject();
    }

    public <T> List<T> findGroundsByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findGroundInstancesByClass(classToFind);
    }

    public <T> Optional<T> findFirstGroundByClass(Class<T> classToFind) {
        return persistentStore.getResolver().findFirstGroundInstanceByClass(classToFind);
    }

    public PillEngine putProperties(Map<String, Object> moreProperties) {
        persistentStore.putAllProperties(moreProperties);
        return this;
    }

    public PillEngine putProperty(String propertyKey, Object propertyValue) {
        persistentStore.putProperty(propertyKey, propertyValue);
        return this;
    }
}
