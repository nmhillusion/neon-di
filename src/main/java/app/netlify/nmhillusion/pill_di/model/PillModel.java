package app.netlify.nmhillusion.pill_di.model;

import app.netlify.nmhillusion.pill_di.annotation.Pillable;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class PillModel {
    private String groundName;
    private Class<?> groundClass;
    private Pillable pillableAnnotation;
    private Object instance;

    public String getGroundName() {
        return groundName;
    }

    public PillModel setGroundName(String groundName) {
        this.groundName = groundName;
        return this;
    }

    public Object getInstance() {
        return instance;
    }

    public PillModel setInstance(Object instance) {
        this.instance = instance;
        return this;
    }

    public Class<?> getGroundClass() {
        return groundClass;
    }

    public PillModel setGroundClass(Class<?> groundClass) {
        this.groundClass = groundClass;
        return this;
    }

    public Pillable getGroundOutAnnotation() {
        return pillableAnnotation;
    }

    public PillModel setGroundOutAnnotation(Pillable pillableAnnotation) {
        this.pillableAnnotation = pillableAnnotation;
        return this;
    }
}
