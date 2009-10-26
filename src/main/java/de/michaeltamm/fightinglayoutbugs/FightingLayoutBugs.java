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

/**
 * Finds layout bugs in a web page by applying all available
 * layout bug detectors to it. These are currently:
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

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) throws Exception {
        final Collection<LayoutBug> result = new ArrayList<LayoutBug>();
        LayoutBugDetector layoutBugDetector;

        layoutBugDetector = new DetectInvalidImageUrls();
        layoutBugDetector.setScreenshotDir(_screenshotDir);
        result.addAll(layoutBugDetector.findLayoutBugsIn(webPage));

        layoutBugDetector = new DetectTextNearOrOverlappingHorizontalEdge();
        layoutBugDetector.setScreenshotDir(_screenshotDir);
        result.addAll(layoutBugDetector.findLayoutBugsIn(webPage));

        layoutBugDetector = new DetectTextNearOrOverlappingVerticalEdge();
        layoutBugDetector.setScreenshotDir(_screenshotDir);
        result.addAll(layoutBugDetector.findLayoutBugsIn(webPage));

        layoutBugDetector = new DetectTextWithTooLowContrast();
        layoutBugDetector.setScreenshotDir(_screenshotDir);
        result.addAll(layoutBugDetector.findLayoutBugsIn(webPage));

        return result;
    }
}
