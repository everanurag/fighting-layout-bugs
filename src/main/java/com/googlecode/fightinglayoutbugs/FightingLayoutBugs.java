/*
 * Copyright 2009-2012 Michael Tamm
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.googlecode.fightinglayoutbugs;

import com.googlecode.fightinglayoutbugs.helpers.ImageHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.impl.*;
import org.apache.log.LogKit;
import org.apache.log.Priority;
import org.apache.log4j.LogManager;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Finds different layout bugs in a web page by executing several
 * {@link LayoutBugDetector}s. By default the following detectors
 * are enabled:<ul>
 *     <li>{@link DetectInvalidImageUrls}</li>
 *     <li>{@link DetectTextNearOrOverlappingHorizontalEdge}</li>
 *     <li>{@link DetectTextNearOrOverlappingVerticalEdge}</li>
 *     <li>{@link DetectTextWithTooLowContrast}</li>
 * </ul>
 */
public class FightingLayoutBugs extends AbstractLayoutBugDetector {

    private static final Log LOG = LogFactory.getLog(FightingLayoutBugs.class);

    private List<Runnable> _runAfterAnalysis = new ArrayList<Runnable>();

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;
    private final List<LayoutBugDetector> _detectors = new ArrayList<LayoutBugDetector>();
    private boolean _debugMode;

    /**
     * Registers the following detectors:<ul>
     *     <li>{@link DetectInvalidImageUrls}</li>
     *     <li>{@link DetectTextNearOrOverlappingHorizontalEdge}</li>
     *     <li>{@link DetectTextNearOrOverlappingVerticalEdge}</li>
     *     <li>{@link DetectTextWithTooLowContrast}</li>
     * </ul>
     */
    public FightingLayoutBugs() {
        this(
            new DetectInvalidImageUrls(),
            new DetectTextNearOrOverlappingHorizontalEdge(),
            new DetectTextNearOrOverlappingVerticalEdge(),
            new DetectTextWithTooLowContrast()
        );
    }

    public FightingLayoutBugs(LayoutBugDetector... detectors) {
        for (LayoutBugDetector d : detectors) {
            enable(d);
        }
    }

    /**
     * Sets the {@link TextDetector} to use.
     */
    public void setTextDetector(TextDetector textDetector) {
        _textDetector = textDetector;
    }

    /**
     * Sets the {@link EdgeDetector} to use.
     */
    public void setEdgeDetector(EdgeDetector edgeDetector) {
        _edgeDetector = edgeDetector;
    }

    /**
     * Call this method to enable the debug mode, which produces
     * more log output and several screenshots of intermediate
     * analysis results.
     */
    public void enableDebugMode() {
        _debugMode = true;
    }

    /**
     * Adds the given detector to the set of detectors, which will be executed,
     * when {@link #findLayoutBugsIn(WebPage) findLayoutBugsIn(...)} is called.
     * If there is already a detector of the same class registered, it will be
     * replaced by the given detector.
     */
    public void enable(LayoutBugDetector detector) {
        if (detector == null) {
            throw new IllegalArgumentException("Method parameter newDetector must not be null.");
        }
        disable(detector.getClass());
        _detectors.add(detector);
    }

    /**
     * Removes all detectors of the given class from the set of detectors, which will be executed,
     * when {@link #findLayoutBugsIn(WebPage) findLayoutBugsIn(...)} is called.
     */
    public void disable(Class<? extends LayoutBugDetector> detectorClass) {
        if (detectorClass == null) {
            throw new IllegalArgumentException("Method parameter detectorClass must not be null.");
        }
        Iterator<LayoutBugDetector> i = _detectors.iterator();
        while (i.hasNext()) {
            LayoutBugDetector detector = i.next();
            if (detector.getClass().isAssignableFrom(detectorClass) || detectorClass.isAssignableFrom(detector.getClass())) {
                i.remove();
            }
        }
    }

    /**
     * Call this method to gain access to one of the {@link LayoutBugDetector}s
     * for calling setter methods on it.
     */
    public <D extends LayoutBugDetector> D configure(Class<D> detectorClass) {
        if (detectorClass == null) {
            throw new IllegalArgumentException("Method parameter detectorClass must not be  null.");
        }
        for (LayoutBugDetector detector : _detectors) {
            if (detectorClass.isAssignableFrom(detector.getClass())) {
                // noinspection unchecked
                return (D) detector;
            }
        }
        throw new IllegalArgumentException("There is no detector of class " + detectorClass.getName());
    }

    /**
     * Runs all registered {@link LayoutBugDetector}s. Before you call this method, you might:<ul>
     * <li>register new detectors via {@link #enable},</li>
     * <li>remove unwanted detectors via {@link #disable},</li>
     * <li>configure a registered detector via {@link #configure},</li>
     * <li>configure the {@link TextDetector} to be used via {@link #setTextDetector},</li>
     * <li>configure the {@link EdgeDetector} to be used via {@link #setEdgeDetector}.</li>
     * </ul>
     */
    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        if (_debugMode) {
            setLogLevelToDebug();
            registerDebugListener();
        }
        try {
            webPage.setTextDetector(_textDetector == null ? new AnimationAwareTextDetector() : _textDetector);
            webPage.setEdgeDetector(_edgeDetector == null ? new SimpleEdgeDetector() : _edgeDetector);
            final Collection<LayoutBug> result = new ArrayList<LayoutBug>();
            for (LayoutBugDetector detector : _detectors) {
                detector.setScreenshotDir(screenshotDir);
                LOG.debug("Running " + detector.getClass().getSimpleName() + " ...");
                result.addAll(detector.findLayoutBugsIn(webPage));
            }
            return result;
        } finally {
            for (Runnable runnable : _runAfterAnalysis) {
                try {
                    runnable.run();
                } catch (RuntimeException e) {
                    LOG.warn(runnable + " failed.", e);
                }
            }
        }
    }

    private void setLogLevelToDebug() {
        String name = FightingLayoutBugs.class.getPackage().getName();
        final Log log = LogFactory.getLog(name);
        if (log instanceof Jdk14Logger || (log instanceof AvalonLogger && ((AvalonLogger) log).getLogger() instanceof org.apache.avalon.framework.logger.Jdk14Logger)) {
            final Logger logger = Logger.getLogger(name);
            final Level originalLevel = logger.getLevel();
            logger.setLevel(Level.FINE);
            _runAfterAnalysis.add(new Runnable() { @Override public void run() {
                logger.setLevel(originalLevel);
            }});
            enableDebugOutputToConsole(logger);
        } else if (log instanceof Log4JLogger || (log instanceof AvalonLogger && ((AvalonLogger) log).getLogger() instanceof org.apache.avalon.framework.logger.Log4JLogger)) {
            final org.apache.log4j.Logger logger = LogManager.getLogger(name);
            final org.apache.log4j.Level originalLevel = logger.getLevel();
            logger.setLevel(org.apache.log4j.Level.DEBUG);
            _runAfterAnalysis.add(new Runnable() { @Override public void run() {
                logger.setLevel(originalLevel);
            }});
        } else if (log instanceof LogKitLogger || (log instanceof AvalonLogger && ((AvalonLogger) log).getLogger() instanceof org.apache.avalon.framework.logger.LogKitLogger)) {
            final org.apache.log.Logger logger = LogKit.getLoggerFor(name);
            final Priority originalLevel = logger.getPriority();
            logger.setPriority(Priority.DEBUG);
            _runAfterAnalysis.add(new Runnable() { @Override public void run() {
                logger.setPriority(originalLevel);
            }});
        } else if (log instanceof SimpleLog) {
            final SimpleLog simpleLog = (SimpleLog) log;
            final int originalLevel = simpleLog.getLevel();
            simpleLog.setLevel(SimpleLog.LOG_LEVEL_DEBUG);
            _runAfterAnalysis.add(new Runnable() { @Override public void run() {
                simpleLog.setLevel(originalLevel);
            }});
        }
    }

    private void enableDebugOutputToConsole(Logger logger) {
        do {
            for (final Handler handler : logger.getHandlers()) {
                if (handler instanceof ConsoleHandler) {
                    final Level originalConsoleLogLevel = handler.getLevel();
                    handler.setLevel(Level.FINE);
                    _runAfterAnalysis.add(new Runnable() { @Override public void run() {
                        handler.setLevel(originalConsoleLogLevel);
                    }});
                }
            }
        } while (logger.getUseParentHandlers() && (logger = logger.getParent()) != null);
    }

    private void registerDebugListener() {
        final AtomicInteger i = new AtomicInteger(0);
        final NumberFormat nf = new DecimalFormat("00");
        final File screenshotDir = this.screenshotDir;
        final Visualization.Listener debugListener = new Visualization.Listener() {
            @Override
            public void algorithmStepFinished(String algorithm, String stepDescription, int[][] tempResult) {
                File pngFile = new File(screenshotDir, nf.format(i.incrementAndGet()) + "_" + algorithm + ".png");
                ImageHelper.pixelsToPngFile(tempResult, pngFile);
                LOG.debug(pngFile.getName() + " -- " + stepDescription);
            }

            @Override
            public void algorithmFinished(String algorithm, String stepDescription, int[][] result) {
                File pngFile = new File(screenshotDir, nf.format(i.incrementAndGet()) + "_" + algorithm + ".png");
                ImageHelper.pixelsToPngFile(result, pngFile);
                LOG.debug(pngFile.getName() + " -- " + stepDescription);
            }
        };
        Visualization.registerListener(debugListener);
        _runAfterAnalysis.add(new Runnable() { @Override public void run() {
            Visualization.unregisterListener(debugListener);
        }});
    }
}
