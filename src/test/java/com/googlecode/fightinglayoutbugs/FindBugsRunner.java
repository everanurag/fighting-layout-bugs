package com.googlecode.fightinglayoutbugs;

import edu.umd.cs.findbugs.*;
import edu.umd.cs.findbugs.classfile.ClassDescriptor;
import edu.umd.cs.findbugs.config.UserPreferences;
import org.apache.commons.io.FilenameUtils;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Suite;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class FindBugsRunner extends Suite {

    private static class AnalyzePackageRunner extends Runner{

        private final List<Runner> _children;
        private final Description _description;

        private AnalyzePackageRunner(String packageName, List<Runner> children) {
            _children = children;
            _description = Description.createSuiteDescription(packageName);
            for (Runner child : _children) {
                _description.addChild(child.getDescription());
            }
        }

        @Override
        public Description getDescription() {
            return _description;
        }

        @Override
        public void run(RunNotifier notifier) {
            for (Runner child : _children) {
                child.run(notifier);
            }
        }

        @Override
        public int testCount() {
            int sum = 0;
            for (Runner child : _children) {
                sum += child.testCount();
            }
            return sum;
        }
    }

    private static class AnalyzeOneClassRunner extends Runner {

        private final Description _description;
        private final List<BugInstance> _bugs;

        private AnalyzeOneClassRunner(String className, List<BugInstance> bugs) {
            _description = Description.createSuiteDescription(className);
            _bugs = bugs;
        }

        @Override
        public Description getDescription() {
            return _description;
        }

        @Override
        public void run(RunNotifier notifier) {
            notifier.fireTestStarted(_description);
            try {
                if (!_bugs.isEmpty()) {
                    StringBuilder sb = new StringBuilder();
                    int n = _bugs.size();
                    sb.append("Found ").append(n).append(n == 1 ? " bug:" : " bugs:");
                    for (BugInstance bug : _bugs) {
                        sb.append("\n").append(bug);
                        String sourceLocation = getSourceLocation(bug);
                        if (sourceLocation != null) {
                            sb.append("\n\tat ").append(sourceLocation);
                        }
                    }
                    AssertionError assertionError = new AssertionError(sb.toString());
                    assertionError.setStackTrace(new StackTraceElement[0]);
                    notifier.fireTestFailure(new Failure(_description, assertionError));
                }
            } catch (Throwable e) {
                notifier.fireTestFailure(new Failure(_description, e));
            }
            notifier.fireTestFinished(_description);
        }

        private String getSourceLocation(BugInstance bug) {
            for (BugAnnotation annotation : bug.getAnnotations()) {
                if (annotation instanceof SourceLineAnnotation) {
                    SourceLineAnnotation sourceLineAnnotation = (SourceLineAnnotation) annotation;
                    return bug.getPrimaryClass().getClassName() + "." + bug.getPrimaryMethod().getMethodName() + "(" + sourceLineAnnotation.getSourceFile() + ":" + sourceLineAnnotation.getStartLine() + ")";
                }
            }
            return bug.getPrimaryClass().getClassName() + "." + bug.getPrimaryMethod().getMethodName() + "(" + bug.getPrimaryClass().getSourceFileName() + ":" + bug.getPrimaryMethod().getSourceLines().getStartLine() + ")";
        }

        @Override
        public int testCount() {
            return 1;
        }
    }

    private static class Bugs {
        Map<String, List<BugInstance>> _bugsByClassName = newHashMap();

        public void add(BugInstance bugInstance) {
            String className = bugInstance.getPrimaryClass().getClassName();
            // If it is an inner class, add it to the bug list of the outer class ...
            int i = className.indexOf('$');
            if (i > 0) {
                className = className.substring(0, i);
            }
            List<BugInstance> list = _bugsByClassName.get(className);
            if (list == null) {
                list = newArrayList();
                _bugsByClassName.put(className, list);
            }
            list.add(bugInstance);
        }

        List<BugInstance> getBugs(String className) {
            List<BugInstance> list = _bugsByClassName.get(className);
            return (list == null ? Collections.<BugInstance>emptyList() : list);
        }
    }

    private static class BugCollector extends AbstractBugReporter {
        private Bugs _bugs = new Bugs();

        private BugCollector() {
            setPriorityThreshold(Detector.NORMAL_PRIORITY);
        }

        @Override
        protected void doReportBug(BugInstance bugInstance) {
            _bugs.add(bugInstance);
        }

        @Override
        public void reportAnalysisError(AnalysisError error) {
            // noinspection ThrowableResultOfMethodCallIgnored
            throw new RuntimeException(error.getMessage(), error.getException());
        }

        @Override
        public void reportMissingClass(String className) {}

        @Override
        public void finish() {}

        @Override
        public BugReporter getRealBugReporter() {
            return this;
        }

        @Override
        public void observeClass(ClassDescriptor classDescriptor) {}

        public Bugs getBugs() {
            return _bugs;
        }
    }

    private static final FileFilter ONLY_DIRS = new FileFilter() { @Override public boolean accept(File f) {
        return f.isDirectory() && !f.getName().startsWith(".");
    }};

    private static final FileFilter ONLY_CLASS_FILES_NO_INNER_CLASSES = new FileFilter() { @Override public boolean accept(File f) {
        return f.isFile() && f.getName().endsWith(".class") && !f.getName().contains("$");
    }};

    private static List<Runner> getRunners(final Class<?> suiteClass) {
        try {
            List<String> classPath = getClassPath(suiteClass);
            File classesDir = getClassesDir(classPath);
            Bugs bugs = analyzeClasses(classesDir, classPath);
            return getRunnersForPackage("", classesDir, bugs);
        } catch (final Exception e) {
            // Initialization failed, return single runner which rethrows the caught exception ...
            final Description description = Description.createSuiteDescription(suiteClass.getSimpleName());
            Runner runner = new Runner() {
                @Override
                public Description getDescription() {
                    return description;
                }

                @Override
                public void run(RunNotifier notifier) {
                    notifier.fireTestStarted(description);
                    notifier.fireTestFailure(new Failure(description, e));
                    notifier.fireTestFinished(description);
                }

                @Override
                public int testCount() {
                    return 1;
                }
            };
            return Collections.singletonList(runner);
        }
    }

    private static List<String> getClassPath(Class<?> suiteClass) {
        List<String> classPath = new ArrayList<String>();
        URLClassLoader classLoader = (URLClassLoader) suiteClass.getClassLoader();
        do {
            for (URL url : classLoader.getURLs()) {
                final String temp = url.toString();
                if (temp.startsWith("file:")) {
                    @SuppressWarnings("deprecation")
                    String path = URLDecoder.decode(temp.substring("file:".length()));
                    classPath.add(path);
                } else {
                    throw new RuntimeException("Don't know how to convert class path URL '" + temp + "' into a path.");
                }
            }
            ClassLoader parentClassLoader = classLoader.getParent();
            classLoader = (parentClassLoader instanceof URLClassLoader && parentClassLoader != classLoader ? (URLClassLoader) parentClassLoader : null);
        } while (classLoader != null);
        return classPath;
    }

    private static Bugs analyzeClasses(File classesDir, List<String> classPath) throws IOException, InterruptedException {
		if (!CheckBcel.check()) {
			throw new RuntimeException("CheckBcel.check() failed.");
		}
        FindBugs2 findBugs = new FindBugs2();
        DetectorFactoryCollection detectorFactoryCollection = DetectorFactoryCollection.instance();
        detectorFactoryCollection.ensureLoaded();
        findBugs.setDetectorFactoryCollection(detectorFactoryCollection);
        BugCollector bugCollector = new BugCollector();
        findBugs.setBugReporter(bugCollector);
        Project project = new Project();
        project.addFile(classesDir.getAbsolutePath());
        for (String path : classPath) {
            project.addAuxClasspathEntry(path);
        }
        project.setCurrentWorkingDirectory(new File("foo").getAbsoluteFile().getParentFile());
        findBugs.setProject(project);
        findBugs.setUserPreferences(UserPreferences.createDefaultUserPreferences());
        findBugs.setClassScreener(new ClassScreener());
        findBugs.finishSettings();
        findBugs.execute();
        return bugCollector.getBugs();
    }

    private static File getClassesDir(List<String> classPath) throws Exception {
        for (String path : classPath) {
            if (path.endsWith("/classes/") || path.endsWith("/classes")) {
                File classesDir = new File(path);
                if (!classesDir.isDirectory()) {
                    throw new RuntimeException(path + " is not an directory.");
                }
                return classesDir;
            }
        }
        throw new RuntimeException("Did not find classes directory in class path: " + classPath);
    }

    private static Runner getRunnerForPackage(String packageName, File packageDir, Bugs bugs) {
        final List<Runner> children = getRunnersForPackage(packageName, packageDir, bugs);
        if (children.isEmpty()) {
            return null;
        } else if (children.size() == 1 && children.get(0) instanceof AnalyzePackageRunner) {
            // Compact empty package ...
            AnalyzePackageRunner child = (AnalyzePackageRunner) children.get(0);
            List<Runner> grandChildren = child._children;
            String childPackageName = child._description.getDisplayName();
            return new AnalyzePackageRunner(childPackageName, grandChildren);
        } else {
            return new AnalyzePackageRunner(packageName, children);
        }
    }

    private static List<Runner> getRunnersForPackage(String packageName, File packageDir, Bugs bugs) {
        final List<Runner> children = new ArrayList<Runner>();
        for (File subDir : packageDir.listFiles(ONLY_DIRS)) {
            Runner child = getRunnerForPackage("".equals(packageName) ? subDir.getName() : packageName + "." + subDir.getName(), subDir, bugs);
            if (child != null) {
                children.add(child);
            }
        }
        for (File classFile : packageDir.listFiles(ONLY_CLASS_FILES_NO_INNER_CLASSES)) {
            String className = packageName + "." + FilenameUtils.getBaseName(classFile.getName());
            Runner runner = new AnalyzeOneClassRunner(className, bugs.getBugs(className));
            children.add(runner);
        }
        return children;
    }

    public FindBugsRunner(Class<?> suiteClass) throws Exception {
        super(suiteClass, getRunners(suiteClass));
    }

}
