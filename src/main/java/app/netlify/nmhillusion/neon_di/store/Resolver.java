package app.netlify.nmhillusion.neon_di.store;

import tech.nmhillusion.n2mix.util.CastUtil;
import tech.nmhillusion.n2mix.util.CollectionUtil;
import tech.nmhillusion.n2mix.validator.StringValidator;
import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.model.NeonModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;


/**
 * date: 2022-02-04
 * <p>
 * created-by: nmhillusion
 */

public class Resolver {
    private final PersistentStore persistentStore;

    protected Resolver(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    @SuppressWarnings("unchecked")
    public <T> Optional<T> getProperty(String propertyKey, Class<T> classToCast) {
        final Object rawPropertyValue = persistentStore.getProperties().get(propertyKey);
        T result = null;

        if (classToCast.isInstance(rawPropertyValue)) {
            result = (T) rawPropertyValue;
        } else {
            if (classToCast.isAssignableFrom(String.class)) {
                result = (T) String.valueOf(rawPropertyValue);
            } else if (classToCast.isAssignableFrom(Integer.class)) {
                result = (T) Integer.valueOf(String.valueOf(rawPropertyValue));
            } else if (classToCast.isAssignableFrom(Long.class)) {
                result = (T) Long.valueOf(String.valueOf(rawPropertyValue));
            } else if (classToCast.isAssignableFrom(Float.class)) {
                result = (T) Float.valueOf(String.valueOf(rawPropertyValue));
            } else if (classToCast.isAssignableFrom(Double.class)) {
                result = (T) Double.valueOf(String.valueOf(rawPropertyValue));
            }
        }

        return Optional.ofNullable(result);
    }

    public <T> List<NeonModel<T>> findNeonsByClass(Class<T> classToFind) {
        final List<NeonModel<T>> resultList = new ArrayList<>();

        for (NeonModel<?> neonModel : persistentStore.getNeonModelList()) {
            if (classToFind.isAssignableFrom(neonModel.getOwnClass())) {
                final Object instance = neonModel.getInstance();
                if (classToFind.isInstance(instance)) {
                    final T castedInstance = classToFind.cast(instance);
                    final NeonModel<T> castedNeon = new NeonModel<T>()
                            .setName(neonModel.getName())
                            .setOwnAnnotation(neonModel.getOwnAnnotation())
                            .setInstance(castedInstance)
                            .setOwnClass(classToFind);

                    resultList.add(castedNeon);
                }
            }
        }

        if (resultList.isEmpty()) {
            getLogger(this).debug("Cannot find instance of [%s] from: [%s]".formatted(classToFind,
                    persistentStore.getNeonModelList()
                            .stream()
                            .map(NeonModel::getOwnClass)
                            .map(Class::getName)
                            .collect(Collectors.joining(","))
            ));
        } else {
            getLogger(this).debug("Found instance of [%s]".formatted(classToFind));
        }

        return resultList;
    }

    public <T> List<T> findNeonInstancesByClass(Class<T> classToFind) {
        return findNeonsByClass(classToFind).stream().map(NeonModel::getInstance).collect(Collectors.toList());
    }

    public <T> Optional<T> findFirstNeonInstanceByClass(Class<T> classToFind) {
        final List<T> foundList = findNeonInstancesByClass(classToFind);
        if (foundList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(foundList.get(0));
        }
    }

    public <T> Optional<T> makeSureOnlyOneNeonInstance(Class<T> classToFind) throws NeonException {
        final List<NeonModel<T>> foundList = findNeonsByClass(classToFind);
        if (foundList.isEmpty()) {
            return Optional.empty();
        } else if (1 < foundList.size()) {
            return Optional.ofNullable(findNeonInstanceWithSmallestNeon(classToFind, foundList));
        } else {
            return Optional.ofNullable(foundList.get(0).getInstance());
        }
    }

    private <T> T findNeonInstanceWithSmallestNeon(Class<T> classToFind, List<NeonModel<T>> foundList) {
        int largestPriority = Integer.MIN_VALUE;
        final List<NeonModel<T>> largestModels = new ArrayList<>();

        for (NeonModel<T> model : foundList) {
            final Neon neon = model.getOwnAnnotation();
            if (neon.priority() > largestPriority) {
                largestPriority = neon.priority();

                largestModels.clear();
                largestModels.add(model);
            } else if (neon.priority() == largestPriority) {
                largestModels.add(model);
            }
        }

        if (1 < largestModels.size()) {
            throw new RuntimeException("Found more than one neon with the largest same priority [" + largestPriority + "]" +
                    " for class " + classToFind +
                    ": " + foundList.stream().map(grd -> grd.getOwnClass().getName()).collect(Collectors.joining(
                    ", ")));
        } else if (largestModels.isEmpty()) {
            throw new RuntimeException("Cannot find most priority neon.");
        } else {
            return largestModels.get(0).getInstance();
        }
    }

    public <T> T fetchParameterValueWithNeon(Inject injectAnnotation, Class<T> clazzToInstance) throws NeonException {
        T classInstance = null;

        if (null != injectAnnotation) {
            {/// Mark: resolve by name
                final String[] itemNames = injectAnnotation.names();
                if (!CollectionUtil.isNullOrEmpty(itemNames)) {
                    for (int nameIndex = 0; nameIndex < itemNames.length && null == classInstance; ++nameIndex) {
                        final String refNameToFetch = itemNames[nameIndex];

                        if (!StringValidator.isBlank(refNameToFetch)) {
                            boolean found = false;
                            for (NeonModel<?> model : persistentStore.getNeonModelList()) {
                                if (model.getName().equals(refNameToFetch)) {
                                    classInstance = CastUtil.safeCast(model.getInstance(), clazzToInstance);
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                throw new RuntimeException("Not found neon with name $neonName".replace(
                                        "$neonName", refNameToFetch));
                            }
                        }
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by properties
                final String propertyKey = injectAnnotation.propertyKey();
                if (!StringValidator.isBlank(propertyKey)) {
                    final Optional<?> propertyValueOptional = getProperty(propertyKey, clazzToInstance);
                    if (propertyValueOptional.isPresent()) {
                        classInstance = CastUtil.safeCast(propertyValueOptional.get(), clazzToInstance);
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by reference classes
                final Class<?>[] referenceClasses = injectAnnotation.referenceClasses();
                final List<Class<?>> classFieldTypeList = new ArrayList<>();

                if (!CollectionUtil.isNullOrEmpty(referenceClasses)) {
                    classFieldTypeList
                            .addAll(Arrays.stream(referenceClasses)
                                    .filter(clazzToInstance::isAssignableFrom)
                                    .toList());
                }
                classFieldTypeList.add(clazzToInstance);

                for (Class<?> orderClassToFetch : classFieldTypeList) {
                    final Optional<?> optionalValue = makeSureOnlyOneNeonInstance(orderClassToFetch);
                    if (optionalValue.isPresent()) {
                        classInstance = CastUtil.safeCast(optionalValue.get(), clazzToInstance);
                        if (null != classInstance) {
                            break;
                        }
                    }
                }
            }
        }
        return classInstance;
    }
}
