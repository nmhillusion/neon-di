package app.netlify.nmhillusion.neon_di.scanner;

import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.store.PersistentStore;
import app.netlify.nmhillusion.pi_logger.PiLoggerHelper;

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

    public void scan(Class<?> startClass) throws URISyntaxException, IOException, NeonException, ClassNotFoundException {
        final List<Class<?>> classList = new ArrayList<>();
        final URL startLocation = startClass.getProtectionDomain().getCodeSource().getLocation();
        PiLoggerHelper.getLog(this).debug("start Neon Engine from start location: " + startLocation);

        if (null != startLocation) {
            final List<URL> packageFiles = findPackages(new File(startLocation.toURI()));
            try (URLClassLoader classLoader = new URLClassLoader(packageFiles.toArray(new URL[0]))) {
                final List<File> classFiles = findClasses(new File(startLocation.toURI()), file -> file.getName().endsWith(".class"));

                if (classFiles.isEmpty()) {
                    throw new NeonException("Resource class files is empty => cannot construct any neon");
                }

                for (File clazzFile : classFiles) {
                    final String packageName = startClass.getPackage().getName();

                    String clazzName = clazzFile.getAbsolutePath()
                            .replaceAll("[\\\\/]", ".");
                    if (clazzName.contains(".")) {
                        clazzName = clazzName.substring(0, clazzName.lastIndexOf("."));
                    }

                    if (clazzName.contains(packageName)) {
                        clazzName = clazzName.substring(clazzName.indexOf(packageName));
                    } else {
                        PiLoggerHelper.getLog(this).warn("ignore for class file: " + clazzFile);
                        break;
                    }

                    final Class<?> aClass = classLoader.loadClass(clazzName);
                    classList.add(aClass);
                }
            }
        } else {
            throw new NeonException("Cannot find start location of neon engine");
        }

        PiLoggerHelper.getLog(this).info("init for classes: " + classList);
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
