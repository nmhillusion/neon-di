package app.netlify.nmhillusion.neon_di.store;

import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.model.NeonModel;
import app.netlify.nmhillusion.neon_di.util.CollectionUtils;
import app.netlify.nmhillusion.neon_di.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<NeonModel> findNeonsByClass(Class<?> classToFind) {
        final List<NeonModel> resultList = new ArrayList<>();

        for (NeonModel neonModel : persistentStore.getPillModelList()) {
            if (classToFind.isAssignableFrom(neonModel.getOwnClass())) {
                final Object instance = neonModel.getInstance();
                if (classToFind.isInstance(instance)) {
                    resultList.add(neonModel);
                }
            }
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findNeonInstancesByClass(Class<T> classToFind) {
        return findNeonsByClass(classToFind).stream().map(grd -> (T) grd.getInstance()).collect(Collectors.toList());
    }

    public <T> Optional<T> findFirstNeonInstanceByClass(Class<T> classToFind) {
        final List<T> foundList = findNeonInstancesByClass(classToFind);
        if (foundList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(foundList.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T makeSureOnlyOneNeonInstance(Class<T> classToFind) {
        final List<NeonModel> foundList = findNeonsByClass(classToFind);
        if (foundList.isEmpty()) {
            throw new RuntimeException("Cannot find neon by class " + classToFind.getName());
        } else if (1 < foundList.size()) {
            return findNeonInstanceWithSmallestNeon(classToFind, foundList);
        } else {
            return (T) foundList.get(0).getInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T findNeonInstanceWithSmallestNeon(Class<T> classToFind, List<NeonModel> foundList) {
        int largestPriority = Integer.MIN_VALUE;
        final List<NeonModel> largestModels = new ArrayList<>();

        for (NeonModel model : foundList) {
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
            return (T) largestModels.get(0).getInstance();
        }
    }

    public Object fetchParameterValueWithNeon(Inject injectAnnotation, Class<?> clazzToInstance) {
        Object classInstance = null;

        if (null != injectAnnotation) {
            {/// Mark: resolve by name
                final String[] itemNames = injectAnnotation.names();
                if (!CollectionUtils.isEmpty(itemNames)) {
                    for (int nameIndex = 0; nameIndex < itemNames.length && null == classInstance; ++nameIndex) {
                        final String refNameToFetch = itemNames[nameIndex];

                        if (!StringUtils.isBlank(refNameToFetch)) {
                            boolean found = false;
                            for (NeonModel model : persistentStore.getPillModelList()) {
                                if (model.getName().equals(refNameToFetch)) {
                                    classInstance = model.getInstance();
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                throw new RuntimeException("Not found neon with name $pillName".replace(
                                        "$pillName", refNameToFetch));
                            }
                        }
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by properties
                final String propertyKey = injectAnnotation.propertyKey();
                if (!StringUtils.isBlank(propertyKey)) {
                    final Optional<?> propertyValueOptional = getProperty(propertyKey, clazzToInstance);
                    if (propertyValueOptional.isPresent()) {
                        classInstance = propertyValueOptional.get();
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by reference classes
                final Class<?>[] referenceClasses = injectAnnotation.referenceClasses();
                final List<Class<?>> classFieldTypeList = new ArrayList<>();

                if (!CollectionUtils.isEmpty(referenceClasses)) {
                    classFieldTypeList
                            .addAll(Arrays.stream(referenceClasses)
                                    .filter(clazzToInstance::isAssignableFrom)
                                    .collect(Collectors.toList()));
                }
                classFieldTypeList.add(clazzToInstance);

                for (Class<?> orderClassToFetch : classFieldTypeList) {
                    classInstance = makeSureOnlyOneNeonInstance(orderClassToFetch);
                    if (null != classInstance) {
                        break;
                    }
                }
            }
        }
        return classInstance;
    }
}
