package tech.nmhillusion.neon_di.inject;

import tech.nmhillusion.neon_di.annotation.Inject;
import tech.nmhillusion.neon_di.annotation.Neon;
import tech.nmhillusion.neon_di.annotation.NeonFactory;
import tech.nmhillusion.neon_di.exception.NeonException;
import tech.nmhillusion.neon_di.model.NeonModel;
import tech.nmhillusion.neon_di.store.PersistentStore;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import static tech.nmhillusion.n2mix.helper.log.LogHelper.getLogger;

/**
 * date: 2022-02-02
 * <p>
 * created-by: nmhillusion
 */

public class Injector {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_INJECT_CLASS = Arrays.asList(
            NeonFactory.class,
            Neon.class
    );
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_INJECT_FIELD = List.of(
            Inject.class
    );
    private final PersistentStore persistentStore;

    public Injector(PersistentStore persistentStore) {
        this.persistentStore = persistentStore;
    }

    public boolean hasAnnotationToFillClass(Class<?> clazz) {
        if (null != clazz) {
            for (Class<? extends Annotation> annotation : ANNOTATIONS_TO_INJECT_CLASS) {
                if (clazz.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasAnnotationToFillField(Field field) {
        if (null != field) {
            for (Class<? extends Annotation> annotation : ANNOTATIONS_TO_INJECT_FIELD) {
                if (field.isAnnotationPresent(annotation)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void inject() throws IllegalAccessException, NeonException {
        for (NeonModel model : persistentStore.getNeonModelList()) {
            if (hasAnnotationToFillClass(model.getOwnClass())) {
                fillGroupToModel(model);
            }
        }
    }

    private void fillGroupToModel(NeonModel model) throws IllegalAccessException, NeonException {
        final Object instance = model.getInstance();
        final Class<?> ownClass = model.getOwnClass();
        final Field[] declaredFields = ownClass.getDeclaredFields();

        for (Field field : declaredFields) {
            if (hasAnnotationToFillField(field)) {
                field.setAccessible(true);

                if (field.canAccess(instance)) {
                    Object valueOfField = null;

                    final Inject injectAnnotation = field.getAnnotation(Inject.class);
                    if (null != injectAnnotation) {
                        valueOfField = persistentStore.getResolver()
                                .fetchParameterValueWithNeon(injectAnnotation, field.getType());
                        field.set(instance, valueOfField);
                    } else {
                        /// Mark: Do nothing because not have @Inject annotation
                    }
                } else {
                    getLogger(this).warn("Cannot access to field " + field.getName() + " of class " + ownClass.getName());
                }
            }
        }
    }
}
