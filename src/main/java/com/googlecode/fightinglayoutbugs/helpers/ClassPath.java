package com.googlecode.fightinglayoutbugs.helpers;

import com.google.common.base.Splitter;
import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.*;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * A list of jar files and directories, which represents the class path
 * of the {@link Thread#getContextClassLoader() current class loader}.
 */
public class ClassPath extends AbstractList<File> {

    private static final Pattern SUREFIREBOOTER_JAR_FILE_NAME_PATTERN = Pattern.compile("surefirebooter[0-9]+\\.jar");

    private final List<File> _files;

    /**
     * Constructs the class path of the {@link Thread#getContextClassLoader() current class loader}.
     */
    public ClassPath() {
        List<File> files = new ArrayList<File>();
        URLClassLoader classLoader = (URLClassLoader) Thread.currentThread().getContextClassLoader();
        do {
            URL[] urls = classLoader.getURLs();
            if (urls.length == 1 && "sun.misc.Launcher$AppClassLoader".equals(classLoader.getClass().getName())) {
                File jarFile = toFile(urls[0]);
                files.add(jarFile);
                files.addAll(readClassPathFromManifestOf(jarFile));
            } else {
                for (URL url : urls) {
                    File file = toFile(url);
                    files.add(file);
                    if (SUREFIREBOOTER_JAR_FILE_NAME_PATTERN.matcher(file.getName()).matches()) {
                        files.addAll(readClassPathFromManifestOf(file));
                    }
                }
            }
            ClassLoader parentClassLoader = classLoader.getParent();
            // noinspection ObjectEquality
            classLoader = (parentClassLoader instanceof URLClassLoader && parentClassLoader != classLoader ? (URLClassLoader) parentClassLoader : null);
        } while (classLoader != null);
        _files = Collections.unmodifiableList(files);
    }

    private File toFile(URL url) {
        String urlString = url.toExternalForm();
        return toFile(urlString);
    }

    private File toFile(String urlString) {
        if (urlString.startsWith("file:")) {
            String path = URLDecoder.decode(urlString.substring("file:".length()));
            return new File(path);
        } else {
            throw new RuntimeException("Don't know how to convert class path URL '" + urlString + "' into a filesystem path.");
        }
    }

    private Collection<File> readClassPathFromManifestOf(File jarFile) {
        try {
            FileInputStream in = new FileInputStream(jarFile);
            try {
                JarInputStream jarInputStream = new JarInputStream(in);
                try {
                    Manifest manifest = jarInputStream.getManifest();
                    List<File> classPath = new ArrayList<File>();
                    for (String s : Splitter.on(" ").split(manifest.getMainAttributes().getValue("Class-Path"))) {
                        File f;
                        if (s.startsWith("file:")) {
                            f = toFile(s);
                        } else {
                            f = new File(jarFile.getParentFile(), s);
                        }
                        classPath.add(f);
                    }
                    return classPath;
                } finally {
                    IOUtils.closeQuietly(jarInputStream);
                }
            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (Exception e) {
            throw new RuntimeException("readClassPathFromManifestOf(" + jarFile + ") failed.", e);
        }
    }

    @Override
    public File get(int index) {
        return _files.get(index);
    }

    @Override
    public int size() {
        return _files.size();
    }
}
