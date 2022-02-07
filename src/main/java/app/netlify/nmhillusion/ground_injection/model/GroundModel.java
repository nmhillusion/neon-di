package app.netlify.nmhillusion.ground_injection.model;

import app.netlify.nmhillusion.ground_injection.annotation.GroundOut;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class GroundModel {
    private String groundName;
    private Class<?> groundClass;
    private GroundOut groundOutAnnotation;
    private Object instance;

    public String getGroundName() {
        return groundName;
    }

    public GroundModel setGroundName(String groundName) {
        this.groundName = groundName;
        return this;
    }

    public Object getInstance() {
        return instance;
    }

    public GroundModel setInstance(Object instance) {
        this.instance = instance;
        return this;
    }

    public Class<?> getGroundClass() {
        return groundClass;
    }

    public GroundModel setGroundClass(Class<?> groundClass) {
        this.groundClass = groundClass;
        return this;
    }

    public GroundOut getGroundOutAnnotation() {
        return groundOutAnnotation;
    }

    public GroundModel setGroundOutAnnotation(GroundOut groundOutAnnotation) {
        this.groundOutAnnotation = groundOutAnnotation;
        return this;
    }
}
