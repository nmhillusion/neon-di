package app.netlify.nmhillusion.pill_di.pool;

import app.netlify.nmhillusion.pill_di.annotation.PillFactory;
import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.model.PillModel;
import app.netlify.nmhillusion.pill_di.store.PersistentStore;
import app.netlify.nmhillusion.pill_di.util.CollectionUtils;
import app.netlify.nmhillusion.pill_di.util.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * date: 2022-02-04
 * <p>
 * created-by: MINGUY1
 */

public class FactoryPopulation {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_FACTORY = Arrays.asList(
            PillFactory.class
    );

    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_CONSTRUCT = Arrays.asList(
            Pillable.class
    );
    private final PersistentStore persistentStore;

    protected FactoryPopulation(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    public boolean hasAnnotationToGroundOfClass(PillModel pillModel) {
        if (null == pillModel
                || null == pillModel.getGroundClass()) {
            return false;
        } else {
            boolean result = false;
            for (Class<? extends Annotation> annotation : ANNOTATIONS_TO_FACTORY) {
                if (pillModel.getGroundClass().isAnnotationPresent(annotation)) {
                    result = true;
                    break;
                }
            }
            return result;
        }
    }

    private boolean hasAnnotationToGroundOfMethod(Method method) {
        if (null == method) {
            return false;
        } else {
            boolean result = false;
            for (Class<? extends Annotation> annotation : ANNOTATIONS_TO_CONSTRUCT) {
                if (method.isAnnotationPresent(annotation)) {
                    result = true;
                    break;
                }
            }
            return result;
        }
    }

    public List<PillModel> populate(PillModel model) throws InvocationTargetException, IllegalAccessException {
        if (hasAnnotationToGroundOfClass(model)) {
            return doPopulate(model);
        } else {
            return Collections.emptyList();
        }
    }

    private List<PillModel> doPopulate(PillModel model) throws InvocationTargetException, IllegalAccessException {
        final List<PillModel> pillModelList = new ArrayList<>();
        final Class<?> groundClass = model.getGroundClass();
        final Method[] publicMethods = groundClass.getMethods();

        if (!CollectionUtils.isEmpty(publicMethods)) {
            for (Method method : publicMethods) {
                if (hasAnnotationToGroundOfMethod(method)) {
                    pillModelList.add(tryToInvokeMethod(model, method));
                }
            }
        }

        return pillModelList;
    }

    private PillModel tryToInvokeMethod(PillModel parentModel, Method method) throws InvocationTargetException,
            IllegalAccessException {
        final PillModel model = new PillModel();

        final List<Object> parameterValues = new ArrayList<>();
        final Parameter[] parameters = method.getParameters();
        if (null == parameters) {
            throw new RuntimeException("Cannot access parameters of method " + method.getName());
        }
        Object newGround = null;
        if (0 != parameters.length) {
            for (Parameter parameter : parameters) {
                final Inject inject = parameter.getAnnotation(Inject.class);
                if (null == inject) {
                    throw new RuntimeException("Cannot wire data without @GroundIn annotation in parameter for method" +
                            " $methodName " +
                            "($parameterType $parameterName)" +
                            " of class $className"
                                    .replace("$methodName", method.getName())
                                    .replace("$parameterType", parameter.getType().getName())
                                    .replace("$parameterName", parameter.getName())
                                    .replace("$className", parentModel.getGroundClass().getName())
                    );
                }

                final Object parameterValue = persistentStore.getResolver().fetchParameterValueWithGroundIn(inject, parameter.getType());
                parameterValues.add(parameterValue);
            }

        }
        newGround = doInvokeMethod(parentModel.getInstance(), method, parameterValues.toArray());

        String groundName = method.getName();
        String annotationGroundName = method.getAnnotation(Pillable.class).name();
        if (!StringUtils.isBlank(annotationGroundName)) {
            groundName = annotationGroundName;
        }

        model
                .setGroundName(groundName)
                .setGroundClass(method.getReturnType())
                .setGroundOutAnnotation(method.getAnnotation(Pillable.class))
                .setInstance(newGround);

        return model;
    }

    private Object doInvokeMethod(Object instance, Method method, Object[] toArray) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, toArray);
    }
}
