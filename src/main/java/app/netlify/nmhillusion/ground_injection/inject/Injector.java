package app.netlify.nmhillusion.ground_injection.inject;

import app.netlify.nmhillusion.ground_injection.annotation.GroundFactory;
import app.netlify.nmhillusion.ground_injection.annotation.GroundIn;
import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;
import app.netlify.nmhillusion.ground_injection.model.GroundModel;
import app.netlify.nmhillusion.ground_injection.store.PersistentStore;
import app.netlify.nmhillusion.ground_injection.util.log.LogHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class Injector {
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_INJECT_CLASS = Arrays.asList(
            GroundFactory.class,
            GroundOut.class
    );
    private static final List<Class<? extends Annotation>> ANNOTATIONS_TO_INJECT_FIELD = Arrays.asList(
            GroundIn.class
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

    public void inject() throws IllegalAccessException {
        for (GroundModel model : persistentStore.getGroundModelList()) {
            if (hasAnnotationToFillClass(model.getGroundClass())) {
                fillGroupToModel(model);
            }
        }
    }

    private void fillGroupToModel(GroundModel model) throws IllegalAccessException {
        final Object instance = model.getInstance();
        final Class<?> groundClass = model.getGroundClass();
        final Field[] declaredFields = groundClass.getDeclaredFields();

        for (Field field : declaredFields) {
            if (hasAnnotationToFillField(field)) {
                field.setAccessible(true);

                if (field.isAccessible()) {
                    Object valueOfField = null;

                    final GroundIn groundInAnnotation = field.getAnnotation(GroundIn.class);
                    if (null != groundInAnnotation) {
                        valueOfField = persistentStore.getResolver()
                                .fetchParameterValueWithGroundIn(groundInAnnotation, field.getType());
                        field.set(instance, valueOfField);
                    } else {
                        /// Mark: Do nothing because not have @GroundIn annotation
                    }
                } else {
                    LogHelper.getLog(this).log("Cannot access to field " + field.getName() + " of class " + groundClass.getName());
                }
            }
        }
    }
}
