package app.netlify.nmhillusion.neon_di.pool;

import app.netlify.nmhillusion.n2mix.util.CastUtil;
import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.annotation.NeonFactory;
import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.model.NeonModel;
import app.netlify.nmhillusion.neon_di.store.PersistentStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static app.netlify.nmhillusion.pi_logger.PiLoggerFactory.getLog;

/**
 * date: 2022-02-02
 * <p>
 * created-by: nmhillusion
 */

public class Population {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_CONSTRUCT = Arrays.asList(
            Neon.class,
            NeonFactory.class
    );
    private static final int MAX_TIMES_TO_RETRY_POPULATE = Integer.MAX_VALUE;
    private final PersistentStore persistentStore;
    private final FactoryPopulation factoryPopulation;


    public Population(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
        this.factoryPopulation = new FactoryPopulation(persistentStore);
    }

    public void populate() throws InvocationTargetException, InstantiationException,
            IllegalAccessException, NeonException {
        final List<Class<?>> allClasses = persistentStore.getScannedClasses();
        if (null != allClasses) {
            final List<Class<?>> waitForPopulateClasses = new CopyOnWriteArrayList<>();

            for (Class<?> clazz : allClasses) {
                if (hasAnnotationToConstruct(clazz)) {
                    waitForPopulateClasses.add(clazz);
                }
            }

            getLog(this).debug("Wait for construction classes: " + waitForPopulateClasses);

            long retryTimesToPopulate = 0;
            while (!waitForPopulateClasses.isEmpty()) {
                retryTimesToPopulate += 1;
                if (retryTimesToPopulate > MAX_TIMES_TO_RETRY_POPULATE) {
                    throw new RuntimeException("Exceed MAX_TIMES_TO_RETRY_POPULATE: " + MAX_TIMES_TO_RETRY_POPULATE);
                }

                for (Class<?> classToPopulate : waitForPopulateClasses) {
                    final boolean populateResult = populateData(classToPopulate);

                    if (populateResult) {
                        waitForPopulateClasses.remove(classToPopulate);
                    }
                }
            }

            retryTimesToPopulate = 0;
            final List<NeonModel<?>> waitForFactoryPopulateClasses = persistentStore.getNeonModelList()
                    .stream()
                    .filter(factoryPopulation::hasAnnotationToInjectOfClass)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            while (!waitForFactoryPopulateClasses.isEmpty()) {
                retryTimesToPopulate += 1;
                if (retryTimesToPopulate > MAX_TIMES_TO_RETRY_POPULATE) {
                    throw new RuntimeException("Exceed MAX_TIMES_TO_RETRY_POPULATE: " + MAX_TIMES_TO_RETRY_POPULATE);
                }

                for (NeonModel<?> model : waitForFactoryPopulateClasses) {
                    try {
                        final List<NeonModel<?>> populateDataList = factoryPopulation.populate(model);
                        persistentStore.getNeonModelList().addAll(populateDataList);

                        waitForFactoryPopulateClasses.remove(model);
                    } catch (Exception ex) {
                        getLog(this).error(ex.getMessage());
                    }
                }
            }

            getLog(this).info("Completed construction for neon list: [%s]".formatted(
                            persistentStore.getNeonModelList()
                                    .stream()
                                    .map(NeonModel::getOwnClass)
                                    .map(Class::getName)
                                    .collect(Collectors.joining(";"))
                    )
            );
        } else {
            getLog(this).error("Cannot find ScannedClasses in persistent storage");
        }
    }

    private boolean populateData(Class<?> clazz) throws InvocationTargetException, InstantiationException,
            IllegalAccessException, NeonException {
        final Constructor<?>[] constructors = clazz.getConstructors();

        final Optional<Constructor<?>> noArgsConstructorOptional =
                Arrays.stream(constructors).filter(constructor -> 0 == constructor.getParameterCount()).findFirst();

        if (noArgsConstructorOptional.isPresent()) {
            return doPopulateData(clazz, noArgsConstructorOptional.get());
        } else {
            boolean result = false;
            for (Constructor<?> constructor : constructors) {
                final Parameter[] parameters = constructor.getParameters();
                final List<Object> parameterValueList = new ArrayList<>();

                for (Parameter parameter : parameters) {
                    final Inject injectAnnotation = parameter.getAnnotation(Inject.class);
                    if (null != injectAnnotation) {
                        final Object parameterValue = persistentStore.getResolver()
                                .fetchParameterValueWithNeon(
                                        injectAnnotation,
                                        parameter.getType());
                        parameterValueList.add(parameterValue);
                    } else {
                        throw new RuntimeException("Cannot wire data to construct without @Inject annotation of " +
                                "class $className and parameter ($parameterType $parameterName)"
                                        .replace("$className", clazz.getName())
                                        .replace("$parameterType", parameter.getType().getName())
                                        .replace("$parameterName", parameter.getName())
                        );
                    }
                }

                if (parameterValueList.stream().allMatch(Objects::nonNull)) {
                    result = doPopulateData(clazz, constructor, parameterValueList.toArray());
                }

                if (result) {
                    break;
                }
            }
            return result;
        }
    }

    private <T> boolean doPopulateData(Class<T> clazz, Constructor<?> constructor, Object... argsToConstructor) throws
            InvocationTargetException, InstantiationException, IllegalAccessException {
        final Neon neonAnnotation = clazz.getAnnotation(Neon.class);
        final T instance = CastUtil.safeCast(constructor.newInstance(argsToConstructor), clazz);

        String name_ = clazz.getName();
        if (null != neonAnnotation) {
            name_ = neonAnnotation.name();
        }

        if (persistentStore.getResolver().findFirstNeonInstanceByClass(clazz).isEmpty()) {
            return persistentStore.getNeonModelList()
                    .add(new NeonModel<T>()
                            .setName(name_)
                            .setOwnClass(clazz)
                            .setOwnAnnotation(neonAnnotation)
                            .setInstance(instance)
                    );
        } else { /// Mark: Existed -> Do not create duplicate
            return true;
        }
    }

    private boolean hasAnnotationToConstruct(Class<?> clazz) {
        boolean result = false;
        for (Class<? extends Annotation> annotationClass : ANNOTATIONS_TO_CONSTRUCT) {
            if (clazz.isAnnotationPresent(annotationClass)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
