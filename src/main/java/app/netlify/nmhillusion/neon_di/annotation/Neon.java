package app.netlify.nmhillusion.neon_di.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Neon {
    String name() default "";

    int priority() default 100;
}