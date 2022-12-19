package app.netlify.nmhillusion.pill_di.inject;

import app.netlify.nmhillusion.pill_di.annotation.PillFactory;
import app.netlify.nmhillusion.pill_di.annotation.Inject;
import app.netlify.nmhillusion.pill_di.annotation.Pillable;
import app.netlify.nmhillusion.pill_di.model.PillModel;
import app.netlify.nmhillusion.pill_di.store.PersistentStore;
import app.netlify.nmhillusion.pi_logger.PiLoggerHelper;

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
			PillFactory.class,
			Pillable.class
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

	public void inject() throws IllegalAccessException {
		for (PillModel model : persistentStore.getPillModelList()) {
			if (hasAnnotationToFillClass(model.getGroundClass())) {
				fillGroupToModel(model);
			}
		}
	}

	private void fillGroupToModel(PillModel model) throws IllegalAccessException {
		final Object instance = model.getInstance();
		final Class<?> groundClass = model.getGroundClass();
		final Field[] declaredFields = groundClass.getDeclaredFields();

		for (Field field : declaredFields) {
			if (hasAnnotationToFillField(field)) {
				field.setAccessible(true);

				if (field.canAccess(instance)) {
					Object valueOfField = null;

					final Inject injectAnnotation = field.getAnnotation(Inject.class);
					if (null != injectAnnotation) {
						valueOfField = persistentStore.getResolver()
								.fetchParameterValueWithGroundIn(injectAnnotation, field.getType());
						field.set(instance, valueOfField);
					} else {
						/// Mark: Do nothing because not have @Inject annotation
					}
				} else {
					PiLoggerHelper.getLog(this).warn("Cannot access to field " + field.getName() + " of class " + groundClass.getName());
				}
			}
		}
	}
}
