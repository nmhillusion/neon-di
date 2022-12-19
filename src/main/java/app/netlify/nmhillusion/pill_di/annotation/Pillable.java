package app.netlify.nmhillusion.pill_di.annotation;

import java.lang.annotation.*;

@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Pillable {
    String name() default "";

    int priority() default 100;
}