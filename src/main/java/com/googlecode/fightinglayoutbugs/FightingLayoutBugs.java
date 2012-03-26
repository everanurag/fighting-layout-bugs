/*
 * Copyright 2009-2011 Michael Tamm
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Finds layout bugs in a web page by executing a certain
 * set of {@link LayoutBugDetector}s. By default the following
 * detectors are enabled:
 * <ul>
 *     <li>{@link DetectInvalidImageUrls}</li>
 *     <li>{@link DetectTextNearOrOverlappingHorizontalEdge}</li>
 *     <li>{@link DetectTextNearOrOverlappingVerticalEdge}</li>
 *     <li>{@link DetectTextWithTooLowContrast}</li>
 * </ul>
 *
 * @author Michael Tamm
 */
public class FightingLayoutBugs extends AbstractLayoutBugDetector {

    private static final Log LOG = LogFactory.getLog(FightingLayoutBugs.class);

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;
    private final List<LayoutBugDetector> _detectors = new ArrayList<LayoutBugDetector>();

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
        webPage.setTextDetector(_textDetector == null ? new AnimationAwareTextDetector() : _textDetector);
        webPage.setEdgeDetector(_edgeDetector == null ? new SimpleEdgeDetector() : _edgeDetector);
        final Collection<LayoutBug> result = new ArrayList<LayoutBug>();
        for (LayoutBugDetector detector : _detectors) {
            detector.setScreenshotDir(screenshotDir);
            LOG.debug("Running " + detector.getClass().getSimpleName() + " ...");
            result.addAll(detector.findLayoutBugsIn(webPage));
        }
        return result;
    }
}
