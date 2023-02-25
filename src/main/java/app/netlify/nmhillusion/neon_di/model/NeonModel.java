package app.netlify.nmhillusion.neon_di.model;

import app.netlify.nmhillusion.neon_di.annotation.Neon;

/**
 * date: 2022-02-02
 * <p>
 * created-by: nmhillusion
 */

public class NeonModel<T> implements Cloneable {
    private String name;
    private Class<T> ownClass;
    private Neon ownAnnotation;
    private T instance;

    public String getName() {
        return name;
    }

    public NeonModel<T> setName(String name) {
        this.name = name;
        return this;
    }

    public Class<T> getOwnClass() {
        return ownClass;
    }

    public NeonModel<T> setOwnClass(Class<T> ownClass) {
        this.ownClass = ownClass;
        return this;
    }

    public Neon getOwnAnnotation() {
        return ownAnnotation;
    }

    public NeonModel<T> setOwnAnnotation(Neon ownAnnotation) {
        this.ownAnnotation = ownAnnotation;
        return this;
    }

    public T getInstance() {
        return instance;
    }

    public NeonModel<T> setInstance(T instance) {
        this.instance = instance;
        return this;
    }

    @Override
    public NeonModel<T> clone() {
        return new NeonModel<T>()
                .setName(name)
                .setInstance(instance)
                .setOwnAnnotation(ownAnnotation)
                .setOwnClass(ownClass);
    }
}
