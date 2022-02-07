package app.netlify.nmhillusion.ground_injection.pool;

import app.netlify.nmhillusion.ground_injection.annotation.GroundFactory;
import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.model.GroundModel;
import app.netlify.nmhillusion.ground_injection.store.PersistentStore;
import app.netlify.nmhillusion.ground_injection.util.log.LogHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class Population {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_CONSTRUCT = Arrays.asList(
            GroundOut.class,
            GroundFactory.class
    );
    private static final int MAX_TIMES_TO_RETRY_POPULATE = 1000;
    private final PersistentStore persistentStore;
    private final FactoryPopulation factoryPopulation;


    public Population(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
        this.factoryPopulation = new FactoryPopulation(persistentStore);
    }

    public void populate() throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        final List<Class<?>> allClasses = persistentStore.getScannedClasses();
        if (null != allClasses) {
            final List<Class<?>> waitForPopulateClasses = new CopyOnWriteArrayList<>();

            for (Class<?> clazz : allClasses) {
                if (hasAnnotationToConstruct(clazz)) {
                    waitForPopulateClasses.add(clazz);
                }
            }

            int retryTimesToPopulate = 0;
            while (!waitForPopulateClasses.isEmpty()) {
                retryTimesToPopulate += 1;
                if (retryTimesToPopulate > MAX_TIMES_TO_RETRY_POPULATE) {
                    throw new RuntimeException("Exceed MAX_TIMES_TO_RETRY_POPULATE: " + MAX_TIMES_TO_RETRY_POPULATE);
                }

                for (Class<?> classToPopulate : waitForPopulateClasses) {
                    final boolean populateResult = populateGround(classToPopulate);

                    if (populateResult) {
                        waitForPopulateClasses.remove(classToPopulate);
                    }
                }
            }

            retryTimesToPopulate = 0;
            final List<GroundModel> waitForFactoryPopulateClasses = persistentStore.getGroundModelList()
                    .stream()
                    .filter(factoryPopulation::hasAnnotationToGroundOfClass)
                    .collect(Collectors.toCollection(CopyOnWriteArrayList::new));
            while (!waitForFactoryPopulateClasses.isEmpty()) {
                retryTimesToPopulate += 1;
                if (retryTimesToPopulate > MAX_TIMES_TO_RETRY_POPULATE) {
                    throw new RuntimeException("Exceed MAX_TIMES_TO_RETRY_POPULATE: " + MAX_TIMES_TO_RETRY_POPULATE);
                }

                for (GroundModel model : waitForFactoryPopulateClasses) {
                    try {
                        final List<GroundModel> populateGroundList = factoryPopulation.populate(model);
                        persistentStore.getGroundModelList().addAll(populateGroundList);

                        waitForFactoryPopulateClasses.remove(model);
                    } catch (Exception ex) {
                        LogHelper.getLog(this).error(ex.getMessage());
                    }
                }
            }
        }
    }

    private boolean populateGround(Class<?> clazz) throws InvocationTargetException, InstantiationException,
            IllegalAccessException {
        final Constructor<?>[] constructors = clazz.getConstructors();

        final Optional<Constructor<?>> noArgsConstructorOptional =
                Arrays.stream(constructors).filter(constructor -> 0 == constructor.getParameterCount()).findFirst();

        if (noArgsConstructorOptional.isPresent()) {
            return doPopulateGround(clazz, noArgsConstructorOptional.get());
        } else {
            boolean result = false;
            for (Constructor<?> constructor : constructors) {
                final Parameter[] parameters = constructor.getParameters();
                final List<Object> parameterValueList = new ArrayList<>();

                for (Parameter parameter : parameters) {
                    final GroundIn groundInAnnotation = parameter.getAnnotation(GroundIn.class);
                    if (null != groundInAnnotation) {
                        final Object parameterValue = persistentStore.getResolver()
                                .fetchParameterValueWithGroundIn(
                                        groundInAnnotation,
                                        parameter.getType());
                        parameterValueList.add(parameterValue);
                    } else {
                        throw new RuntimeException("Cannot wire data to construct without @GroundIn annotation of " +
                                "class $className and parameter ($parameterType $parameterName)"
                                        .replace("$className", clazz.getName())
                                        .replace("$parameterType", parameter.getType().getName())
                                        .replace("$parameterName", parameter.getName())
                        );
                    }
                }

                if (parameterValueList.stream().allMatch(Objects::nonNull)) {
                    result = doPopulateGround(clazz, constructor, parameterValueList.toArray());
                }

                if (result) {
                    break;
                }
            }
            return result;
        }
    }

    private boolean doPopulateGround(Class<?> clazz, Constructor<?> constructor, Object... argsToConstructor) throws
            InvocationTargetException, InstantiationException, IllegalAccessException {
        final GroundOut groundOutAnnotation = clazz.getAnnotation(GroundOut.class);
        final Object instance = constructor.newInstance(argsToConstructor);

        String groundName = clazz.getName();
        if (null != groundOutAnnotation) {
            groundName = groundOutAnnotation.name();
        }

        if (!persistentStore.getResolver().findFirstGroundInstanceByClass(clazz).isPresent()) {
            return persistentStore.getGroundModelList()
                    .add(new GroundModel()
                            .setGroundName(groundName)
                            .setGroundClass(clazz)
                            .setGroundOutAnnotation(groundOutAnnotation)
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
