package app.netlify.nmhillusion.ground_injection.store;

import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.model.GroundModel;
import app.netlify.nmhillusion.ground_injection.util.CollectionUtils;
import app.netlify.nmhillusion.ground_injection.util.StringUtils;

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

    public List<GroundModel> findGroundsByClass(Class<?> classToFind) {
        final List<GroundModel> resultList = new ArrayList<>();

        for (GroundModel groundModel : persistentStore.getGroundModelList()) {
            if (classToFind.isAssignableFrom(groundModel.getGroundClass())) {
                final Object instance = groundModel.getInstance();
                if (classToFind.isInstance(instance)) {
                    resultList.add(groundModel);
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
        final List<GroundModel> foundList = findGroundsByClass(classToFind);
        if (foundList.isEmpty()) {
            throw new RuntimeException("Cannot find ground by class " + classToFind.getName());
        } else if (1 < foundList.size()) {
            return findGroundInstanceWithSmallestGround(classToFind, foundList);
        } else {
            return (T) foundList.get(0).getInstance();
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T findGroundInstanceWithSmallestGround(Class<T> classToFind, List<GroundModel> foundList) {
        int smallestPriority = Integer.MAX_VALUE;
        final List<GroundModel> smallestModels = new ArrayList<>();

        for (GroundModel model : foundList) {
            final GroundOut groundOut = model.getGroundOutAnnotation();
            if (groundOut.priority() < smallestPriority) {
                smallestPriority = groundOut.priority();

                smallestModels.clear();
                smallestModels.add(model);
            } else if (groundOut.priority() == smallestPriority) {
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

    public Object fetchParameterValueWithGroundIn(GroundIn groundInAnnotation, Class<?> clazzToInstance) {
        Object classInstance = null;

        if (null != groundInAnnotation) {
            {/// Mark: resolve by groundName
                final String[] groundNames = groundInAnnotation.groundNames();
                if (!CollectionUtils.isEmpty(groundNames)) {
                    for (int groundNameIndex = 0; groundNameIndex < groundNames.length && null == classInstance; ++groundNameIndex) {
                        final String groundNameToFetch = groundNames[groundNameIndex];

                        if (!StringUtils.isBlank(groundNameToFetch)) {
                            boolean found = false;
                            for (GroundModel model : persistentStore.getGroundModelList()) {
                                if (model.getGroundName().equals(groundNameToFetch)) {
                                    classInstance = model.getInstance();
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                throw new RuntimeException("Not found ground with name $groundName".replace(
                                        "$groundName", groundNameToFetch));
                            }
                        }
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by properties
                final String propertyKey = groundInAnnotation.propertyKey();
                if (!StringUtils.isBlank(propertyKey)) {
                    final Optional<?> propertyValueOptional = getProperty(propertyKey, clazzToInstance);
                    if (propertyValueOptional.isPresent()) {
                        classInstance = propertyValueOptional.get();
                    }
                }
            }

            if (null == classInstance) {/// Mark: resolve by reference classes
                final Class<?>[] referenceClasses = groundInAnnotation.referenceClasses();
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
