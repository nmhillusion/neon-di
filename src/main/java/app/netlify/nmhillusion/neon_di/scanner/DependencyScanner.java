package app.netlify.nmhillusion.neon_di.scanner;

import app.netlify.nmhillusion.neon_di.store.PersistentStore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

/**
 * date: 2022-01-27
 * <p>
 * created-by: nmhillusion
 */

public class DependencyScanner {
	private final PersistentStore persistentStore;

	public DependencyScanner(PersistentStore persistentStore) {
		this.persistentStore = persistentStore;
	}

	public void scan(Class<?> startClass) throws URISyntaxException, MalformedURLException {
		final List<Class<?>> classList = new ArrayList<>();
		final URL resource = startClass.getClassLoader().getResource(".");
		if (null != resource) {
			final List<URL> packageFiles = findPackages(new File(resource.toURI()));
			try (URLClassLoader classLoader = new URLClassLoader(packageFiles.toArray(new URL[0]))) {
				final List<File> classFiles = findClasses(new File(resource.toURI()), file -> file.getName().endsWith(".class"));
				classFiles.forEach(clazzFile -> {
					try {
						final String packageName = startClass.getPackage().getName();

						String clazzName = clazzFile.getAbsolutePath()
								.replaceAll("[\\\\/]", ".");
						clazzName = clazzName.substring(0, clazzName.lastIndexOf("."));
						clazzName = clazzName.substring(clazzName.indexOf(packageName));

						final Class<?> aClass = classLoader.loadClass(clazzName);
						classList.add(aClass);
					} catch (Exception e) {
						e.printStackTrace();
					}
				});
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		persistentStore.getScannedClasses().clear();
		persistentStore.getScannedClasses().addAll(classList);
	}

	private List<URL> findPackages(File rootFile) throws MalformedURLException {
		if (null == rootFile) {
			return Collections.emptyList();
		} else if (!rootFile.isDirectory()) {
			return Collections.emptyList();
		} else {
			final List<URL> resultList = new ArrayList<>();
			resultList.add(rootFile.toURI().toURL());

			final File[] fileList = rootFile.listFiles();
			if (null != fileList) {
				for (File iFile : fileList) {
					resultList.addAll(findPackages(iFile));
				}
			}
			return resultList;
		}
	}

	private List<File> findClasses(File rootFile, Predicate<File> condition) {
		if (null == rootFile) {
			return Collections.emptyList();
		} else {
			if (rootFile.isFile()) {
				if (condition.test(rootFile)) {
					return Collections.singletonList(rootFile);
				} else {
					return Collections.emptyList();
				}
			} else {
				final List<File> resultList = new ArrayList<>();
				final File[] childrenFiles = rootFile.listFiles();
				if (null != childrenFiles) {
					for (File childFile : childrenFiles) {
						resultList.addAll(
								findClasses(childFile, condition)
						);
					}
				}
				return resultList;
			}
		}
	}
}
