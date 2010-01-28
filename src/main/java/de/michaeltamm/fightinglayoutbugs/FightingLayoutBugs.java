/*
 * Copyright 2009 Michael Tamm
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

package de.michaeltamm.fightinglayoutbugs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Finds layout bugs in a web page by applying all available
 * layout bug detectors to it. These are currently:
 * <ul>
 *     <li>{@link DetectNeedsHorizontalScrolling}</li>
 *     <li>{@link DetectInvalidImageUrls}</li>
 *     <li>{@link DetectTextNearOrOverlappingHorizontalEdge}</li>
 *     <li>{@link DetectTextNearOrOverlappingVerticalEdge}</li>
 *     <li>{@link DetectTextWithTooLowContrast}</li>
 * </ul>
 *
 * @author Michael Tamm
 */
public class FightingLayoutBugs extends AbstractLayoutBugDetector {

    private final Map<Class<? extends LayoutBugDetector>, LayoutBugDetector> _layoutBugDetectors;

    private TextDetector _textDetector;
    private EdgeDetector _edgeDetector;

    public FightingLayoutBugs() {
        _layoutBugDetectors = new LinkedHashMap<Class<? extends LayoutBugDetector>, LayoutBugDetector>();
        // The first detector should be DetectNeedsHorizontalScrolling, because it resizes the browser window ...  
        _layoutBugDetectors.put(DetectNeedsHorizontalScrolling.class, new DetectNeedsHorizontalScrolling());
        _layoutBugDetectors.put(DetectInvalidImageUrls.class, new DetectInvalidImageUrls());
        _layoutBugDetectors.put(DetectTextNearOrOverlappingHorizontalEdge.class, new DetectTextNearOrOverlappingHorizontalEdge());
        _layoutBugDetectors.put(DetectTextNearOrOverlappingVerticalEdge.class, new DetectTextNearOrOverlappingVerticalEdge());
        _layoutBugDetectors.put(DetectTextWithTooLowContrast.class, new DetectTextWithTooLowContrast());
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
     * Call this method to gain access to one of the {@link LayoutBugDetector}s
     * for calling setter methods on it.
     */
    public <D extends LayoutBugDetector> D configure(Class<D> detectorClass) {
        return (D) _layoutBugDetectors.get(detectorClass);
    }

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) throws Exception {
        if (_textDetector != null) {
            webPage.setTextDetector(_textDetector);
        }
        if (_edgeDetector != null) {
            webPage.setEdgeDetector(_edgeDetector);
        }
        final Collection<LayoutBug> result = new ArrayList<LayoutBug>();
        for (LayoutBugDetector detector : _layoutBugDetectors.values()) {
            detector.setScreenshotDir(_screenshotDir);
            result.addAll(detector.findLayoutBugsIn(webPage));
        }
        return result;
    }
}
