package app.netlify.nmhillusion.neon_di.model;

import app.netlify.nmhillusion.neon_di.annotation.Neon;

/**
 * date: 2022-02-02
 * <p>
 * created-by: MINGUY1
 */

public class NeonModel {
	private String name;
	private Class<?> ownClass;
	private Neon ownAnnotation;
	private Object instance;

	public String getName() {
		return name;
	}

	public NeonModel setName(String name) {
		this.name = name;
		return this;
	}

	public Object getInstance() {
		return instance;
	}

	public NeonModel setInstance(Object instance) {
		this.instance = instance;
		return this;
	}

	public Class<?> getOwnClass() {
		return ownClass;
	}

	public NeonModel setOwnClass(Class<?> ownClass) {
		this.ownClass = ownClass;
		return this;
	}

	public Neon getOwnAnnotation() {
		return ownAnnotation;
	}

	public NeonModel setOwnAnnotation(Neon ownAnnotation) {
		this.ownAnnotation = ownAnnotation;
		return this;
	}
}
