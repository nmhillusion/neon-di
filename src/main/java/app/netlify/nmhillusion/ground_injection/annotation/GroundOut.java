package app.netlify.nmhillusion.ground_injection.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GroundOut {
    String name() default "";

    int priority() default 100;
}
