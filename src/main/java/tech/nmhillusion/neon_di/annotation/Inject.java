package tech.nmhillusion.neon_di.annotation;

import java.lang.annotation.*;

/**
 * date: 2022-02-01
 * <p>
 * created-by: nmhillusion
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
	String[] names() default {};

	Class[] referenceClasses() default {};

	String propertyKey() default "";
}
