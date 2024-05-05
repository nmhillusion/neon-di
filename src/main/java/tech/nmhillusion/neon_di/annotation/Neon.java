package tech.nmhillusion.neon_di.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Neon {
    String name() default "";

    /**
     * larger value will take priority
     */
    int priority() default 100;
}