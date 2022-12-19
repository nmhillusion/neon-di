package app.netlify.nmhillusion.pill_di.store;

import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.model.PillModel;
import app.netlify.nmhillusion.pill_di.util.CollectionUtils;
import app.netlify.nmhillusion.pill_di.util.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
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

    public List<PillModel> findGroundsByClass(Class<?> classToFind) {
        final List<PillModel> resultList = new ArrayList<>();

        for (PillModel pillModel : persistentStore.getPillModelList()) {
            if (classToFind.isAssignableFrom(pillModel.getGroundClass())) {
                final Object instance = pillModel.getInstance();
                if (classToFind.isInstance(instance)) {
                    resultList.add(pillModel);
                }
            }
        }
        return resultList;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> findGroundInstancesByClass(Class<T> classToFind) {
        return findGroundsByClass(classToFind).stream().map(grd -> (T) grd.getInstance()).collect(Collectors.toList());
    }

    public <T> Optional<T> findFirstGroundInstanceByClass(Class<T> classToFind) {
        final List<T> foundList = findGroundInstancesByClass(classToFind);
        if (foundList.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(foundList.get(0));
        }
    }

    @SuppressWarnings("unchecked")
    public <T> T makeSureOnlyOneGroundInstance(Class<T> classToFind) {
        final List<PillModel> foundList = findGroundsByClass(classToFind);
        if (foundList.isEmpty()) {
            throw new RuntimeException("Cannot find ground by class " + classToFind.getName());
        } else if (1 < foundList.size()) {
            return findGroundInstanceWithSmallestGround(classToFind, foundList);
        } else {
            return (T) foundList.get(0).getInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T findGroundInstanceWithSmallestGround(Class<T> classToFind, List<PillModel> foundList) {
        int smallestPriority = Integer.MAX_VALUE;
        final List<PillModel> smallestModels = new ArrayList<>();

        for (PillModel model : foundList) {
            final Pillable pillable = model.getGroundOutAnnotation();
            if (pillable.priority() < smallestPriority) {
                smallestPriority = pillable.priority();

                smallestModels.clear();
                smallestModels.add(model);
            } else if (pillable.priority() == smallestPriority) {
                smallestModels.add(model);
            }
        }

        if (1 < smallestModels.size()) {
            throw new RuntimeException("Found more than one ground with the smallest same priority [" + smallestPriority + "]" +
                    " for class " + classToFind +
                    ": " + foundList.stream().map(grd -> grd.getGroundClass().getName()).collect(Collectors.joining(
                    ", ")));
        } else if (smallestModels.isEmpty()) {
            throw new RuntimeException("Cannot find most priority ground.");
        } else {
            return (T) smallestModels.get(0).getInstance();
        }
    }

    public Object fetchParameterValueWithGroundIn(Inject injectAnnotation, Class<?> clazzToInstance) {
        Object classInstance = null;

        if (null != injectAnnotation) {
            {/// Mark: resolve by pillName
                final String[] pillNames = injectAnnotation.pillNames();
                if (!CollectionUtils.isEmpty(pillNames)) {
                    for (int pillNameIndex = 0; pillNameIndex < pillNames.length && null == classInstance; ++pillNameIndex) {
                        final String pillNameToFetch = pillNames[pillNameIndex];

                        if (!StringUtils.isBlank(pillNameToFetch)) {
                            boolean found = false;
                            for (PillModel model : persistentStore.getPillModelList()) {
                                if (model.getGroundName().equals(pillNameToFetch)) {
                                    classInstance = model.getInstance();
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                throw new RuntimeException("Not found ground with name $pillName".replace(
                                        "$pillName", pillNameToFetch));
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
                    classInstance = makeSureOnlyOneGroundInstance(orderClassToFetch);
                    if (null != classInstance) {
                        break;
                    }
                }
            }
        }
        return classInstance;
    }
}
