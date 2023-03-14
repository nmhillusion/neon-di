package app.netlify.nmhillusion.neon_di.scanner;

import app.netlify.nmhillusion.n2mix.helper.log.LogHelper;
import app.netlify.nmhillusion.neon_di.exception.NeonException;
import app.netlify.nmhillusion.neon_di.store.PersistentStore;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static app.netlify.nmhillusion.pi_logger.PiLoggerFactory.getLog;

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

    public Set<String> getClassNamesFromJarFile(File givenFile) throws IOException {
        Set<String> classNames = new HashSet<>();
        try (JarFile jarFile = new JarFile(givenFile)) {
            Enumeration<JarEntry> e = jarFile.entries();
            while (e.hasMoreElements()) {
                JarEntry jarEntry = e.nextElement();
                if (jarEntry.getName().endsWith(".class")) {
                    String className = jarEntry.getName()
                            .replace("/", ".")
                            .replace(".class", "");
                    classNames.add(className);
                }
            }
            return classNames;
        }
    }

    public void scan(Class<?> startClass) throws URISyntaxException, IOException, NeonException, ClassNotFoundException {
        if (null == startClass) {
            throw new NeonException("startClass is null");
        }

        final List<Class<?>> classList = new ArrayList<>();
        final String packageName = startClass.getPackage().getName();
        final URL startLocation = startClass.getProtectionDomain().getCodeSource().getLocation();
        getLog(this).debug("start Neon Engine from start location: " + startLocation);

        if (null != startLocation) {
            final List<URL> packageFiles = findPackages(new File(startLocation.toURI()));
            try (URLClassLoader classLoader = new URLClassLoader(packageFiles.toArray(new URL[0]))) {
                final List<File> classFiles_ = findClasses(new File(startLocation.toURI()), file -> file.getName().endsWith(".class"));
                final List<String> classNameCollection = new ArrayList<>();

                if (!classFiles_.isEmpty()) {
                    LogHelper.getLog(this).infoFormat("scan class from directory, size: %s ", classFiles_.size());
                    classNameCollection.addAll(
                            classFiles_.stream()
                                    .map(File::getAbsolutePath)
                                    .toList()
                    );
                } else if (startLocation.getFile().endsWith(".jar") || startLocation.getFile().endsWith(".exe")) {
                    final Set<String> classNamesFromJarFile = getClassNamesFromJarFile(new File(startLocation.toURI()));
                    LogHelper.getLog(this).infoFormat("scan class from jar file, size: %s ", classNamesFromJarFile.size());
                    classNameCollection.addAll(classNamesFromJarFile);
                }

                if (classNameCollection.isEmpty()) {
                    throw new NeonException("Resource class files is empty => cannot construct any neon, from start location: " + startLocation);
                }

                for (String clazzName : classNameCollection) {
                    if (clazzName.contains(packageName)) {
                        LogHelper.getLog(this).traceFormat(">> started loading class name: %s", clazzName);

                        clazzName = clazzName
                                .replaceAll("[\\\\/]", ".");
                        if (clazzName.contains(".class")) {
                            clazzName = clazzName.substring(0, clazzName.lastIndexOf(".class"));
                        }


                        clazzName = clazzName.substring(clazzName.indexOf(packageName));

                        LogHelper.getLog(this).traceFormat("<< loaded class name: %s", clazzName);

                        final Class<?> aClass = classLoader.loadClass(clazzName);
                        classList.add(aClass);
                    } else {
                        getLog(this).debug("ignore for class file not in this package: " + clazzName);
                    }
                }
            }
        } else {
            throw new NeonException("Cannot find start location of neon engine");
        }

        getLog(this).info("init for classes: " + classList);
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
