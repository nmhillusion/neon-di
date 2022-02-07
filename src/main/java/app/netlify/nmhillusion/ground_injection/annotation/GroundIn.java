package app.netlify.nmhillusion.ground_injection.annotation;

import java.lang.annotation.*;

/**
 * date: 2022-02-01
 * <p>
 * created-by: MINGUY1
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface GroundIn {
    String[] groundNames() default {};

    Class[] referenceClasses() default {};

    String propertyKey() default "";
}
