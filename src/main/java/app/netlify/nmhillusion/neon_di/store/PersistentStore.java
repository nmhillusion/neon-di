package app.netlify.nmhillusion.neon_di.store;

import app.netlify.nmhillusion.neon_di.model.NeonModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * date: 2022-02-02
 * <p>
 * created-by: nmhillusion
 */

public class PersistentStore {
	private final Map<String, Object> properties = new TreeMap<>();
	private final Resolver resolver;
	private List<Class<?>> scannedClasses = new ArrayList<>();
	private List<NeonModel> neonModelList = new ArrayList<>();

	public PersistentStore() {
		resolver = new Resolver(this);
	}

	public Resolver getResolver() {
		return resolver;
	}

	public List<Class<?>> getScannedClasses() {
		return scannedClasses;
	}

	public PersistentStore setScannedClasses(List<Class<?>> scannedClasses) {
		this.scannedClasses = scannedClasses;
		return this;
	}

	public List<NeonModel> getNeonModelList() {
		return neonModelList;
	}

	public PersistentStore setNeonModelList(List<NeonModel> neonModelList) {
		this.neonModelList = neonModelList;
		return this;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void putProperty(String propertyKey, Object propertyValue) {
		getProperties().put(propertyKey, propertyValue);
	}

	public void putAllProperties(Map<String, Object> moreProperties) {
		getProperties().putAll(moreProperties);
	}
}
