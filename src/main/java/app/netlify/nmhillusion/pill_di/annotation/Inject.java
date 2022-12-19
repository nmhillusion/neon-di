package app.netlify.nmhillusion.pill_di.annotation;

import java.lang.annotation.*;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Inject {
    String[] pillNames() default {};

    Class[] referenceClasses() default {};

    String propertyKey() default "";
}
