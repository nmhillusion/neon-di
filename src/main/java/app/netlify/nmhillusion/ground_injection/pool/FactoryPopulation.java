package app.netlify.nmhillusion.ground_injection.pool;

import app.netlify.nmhillusion.ground_injection.annotation.GroundFactory;
import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.model.GroundModel;
import app.netlify.nmhillusion.ground_injection.store.PersistentStore;
import app.netlify.nmhillusion.ground_injection.util.CollectionUtils;
import app.netlify.nmhillusion.ground_injection.util.StringUtils;

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
            GroundFactory.class
    );

    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_CONSTRUCT = Arrays.asList(
            GroundOut.class
    );
    private final PersistentStore persistentStore;

    protected FactoryPopulation(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    public boolean hasAnnotationToGroundOfClass(GroundModel groundModel) {
        if (null == groundModel
                || null == groundModel.getGroundClass()) {
            return false;
        } else {
            boolean result = false;
            for (Class<? extends Annotation> annotation : ANNOTATIONS_TO_FACTORY) {
                if (groundModel.getGroundClass().isAnnotationPresent(annotation)) {
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

    public List<GroundModel> populate(GroundModel model) throws InvocationTargetException, IllegalAccessException {
        if (hasAnnotationToGroundOfClass(model)) {
            return doPopulate(model);
        } else {
            return Collections.emptyList();
        }
    }

    private List<GroundModel> doPopulate(GroundModel model) throws InvocationTargetException, IllegalAccessException {
        final List<GroundModel> groundModelList = new ArrayList<>();
        final Class<?> groundClass = model.getGroundClass();
        final Method[] publicMethods = groundClass.getMethods();

        if (!CollectionUtils.isEmpty(publicMethods)) {
            for (Method method : publicMethods) {
                if (hasAnnotationToGroundOfMethod(method)) {
                    groundModelList.add(tryToInvokeMethod(model, method));
                }
            }
        }

        return groundModelList;
    }

    private GroundModel tryToInvokeMethod(GroundModel parentModel, Method method) throws InvocationTargetException,
            IllegalAccessException {
        final GroundModel model = new GroundModel();

        final List<Object> parameterValues = new ArrayList<>();
        final Parameter[] parameters = method.getParameters();
        if (null == parameters) {
            throw new RuntimeException("Cannot access parameters of method " + method.getName());
        }
        Object newGround = null;
        if (0 != parameters.length) {
            for (Parameter parameter : parameters) {
                final GroundIn groundIn = parameter.getAnnotation(GroundIn.class);
                if (null == groundIn) {
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

                final Object parameterValue = persistentStore.getResolver().fetchParameterValueWithGroundIn(groundIn, parameter.getType());
                parameterValues.add(parameterValue);
            }

        }
        newGround = doInvokeMethod(parentModel.getInstance(), method, parameterValues.toArray());

        String groundName = method.getName();
        String annotationGroundName = method.getAnnotation(GroundOut.class).name();
        if (!StringUtils.isBlank(annotationGroundName)) {
            groundName = annotationGroundName;
        }

        model
                .setGroundName(groundName)
                .setGroundClass(method.getReturnType())
                .setGroundOutAnnotation(method.getAnnotation(GroundOut.class))
                .setInstance(newGround);

        return model;
    }

    private Object doInvokeMethod(Object instance, Method method, Object[] toArray) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, toArray);
    }
}
