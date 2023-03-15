package app.netlify.nmhillusion.neon_di.pool;

import app.netlify.nmhillusion.n2mix.util.CollectionUtil;
import app.netlify.nmhillusion.n2mix.validator.StringValidator;
import app.netlify.nmhillusion.neon_di.annotation.Inject;
import app.netlify.nmhillusion.neon_di.annotation.Neon;
import app.netlify.nmhillusion.neon_di.annotation.NeonFactory;
import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.model.NeonModel;
import app.netlify.nmhillusion.neon_di.store.PersistentStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * date: 2022-02-04
 * <p>
 * created-by: nmhillusion
 */

public class FactoryPopulation {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_FACTORY = List.of(
            NeonFactory.class
    );

    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_CONSTRUCT = List.of(
            Neon.class
    );
    private final PersistentStore persistentStore;

    protected FactoryPopulation(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    public boolean hasAnnotationToInjectOfClass(NeonModel<?> neonModel) {
        if (null == neonModel
                || null == neonModel.getOwnClass()) {
            return false;
        } else {
            boolean result = false;
            for (Class<? extends Annotation> annotation_ : ANNOTATIONS_TO_FACTORY) {
                if (neonModel.getOwnClass().isAnnotationPresent(annotation_)) {
                    result = true;
                    break;
                }
            }
            return result;
        }
    }

    private boolean hasAnnotationToInjectOfMethod(Method method) {
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

    public List<NeonModel<?>> populate(NeonModel<?> model) throws InvocationTargetException, IllegalAccessException, NeonException {
        if (hasAnnotationToInjectOfClass(model)) {
            return doPopulate(model);
        } else {
            return Collections.emptyList();
        }
    }

    private List<NeonModel<?>> doPopulate(NeonModel<?> model) throws InvocationTargetException, IllegalAccessException, NeonException {
        final List<NeonModel<?>> neonModelList = new ArrayList<>();
        final Class<?> clazz = model.getOwnClass();
        final Method[] publicMethods = clazz.getMethods();

        if (!CollectionUtil.isNullOrEmpty(publicMethods)) {
            for (Method method : publicMethods) {
                if (hasAnnotationToInjectOfMethod(method)) {
                    neonModelList.add(tryToInvokeMethod(model, method));
                }
            }
        }

        return neonModelList;
    }

    @SuppressWarnings("unchecked")
    private NeonModel<?> tryToInvokeMethod(NeonModel<?> parentModel, Method method) throws InvocationTargetException,
            IllegalAccessException, NeonException {
        final NeonModel<Object> model = new NeonModel<>();

        final List<Object> parameterValues = new ArrayList<>();
        final Parameter[] parameters = method.getParameters();
        if (null == parameters) {
            throw new RuntimeException("Cannot access parameters of method " + method.getName());
        }
        Object newNeon = null;
        for (Parameter parameter : parameters) {
            final Inject inject = parameter.getAnnotation(Inject.class);
            if (null == inject) {
                throw new RuntimeException("Cannot wire data without @Neon annotation in parameter for method" +
                        " $methodName " +
                        "($parameterType $parameterName)" +
                        " of class $className"
                                .replace("$methodName", method.getName())
                                .replace("$parameterType", parameter.getType().getName())
                                .replace("$parameterName", parameter.getName())
                                .replace("$className", parentModel.getOwnClass().getName())
                );
            }

            final Object parameterValue = persistentStore.getResolver().fetchParameterValueWithNeon(inject, parameter.getType());
            parameterValues.add(parameterValue);
        }

        newNeon = doInvokeMethod(parentModel.getInstance(), method, parameterValues.toArray());

        String name = method.getName();
        String annotationName = method.getAnnotation(Neon.class).name();
        if (!StringValidator.isBlank(annotationName)) {
            name = annotationName;
        }

        model
                .setName(name)
                .setOwnClass((Class<Object>) method.getReturnType())
                .setOwnAnnotation(method.getAnnotation(Neon.class))
                .setInstance(newNeon);

        return model;
    }

    private Object doInvokeMethod(Object instance, Method method, Object[] toArray) throws InvocationTargetException, IllegalAccessException {
        return method.invoke(instance, toArray);
    }
}
