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

import org.openqa.selenium.firefox.FirefoxDriver;

import java.util.Collection;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * @author Michael Tamm
 */
public class DetectTextNearOrOverlappingVerticalEdge extends AbstractLayoutBugDetector {

    public Collection<LayoutBug> findLayoutBugs(FirefoxDriver driver) throws Exception {
        final TextDetector textDetector = new SimpleTextDetector();
        final boolean[][] text = textDetector.detectTextPixelsIn(driver);
        final int w = text.length;
        final int h = text[0].length;
        if (w > 0 && h > 0) {
            final EdgeDetector edgeDetector = new SimpleEdgeDetector();
            final boolean[][] verticalEdges = edgeDetector.detectVerticalEdgesIn(driver, 16);
            assert verticalEdges.length == w;
            assert verticalEdges[0].length == h;
            final boolean[][] buggyPixels = new boolean[w][h];
            boolean foundBuggyPixel = false;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (text[x][y] && verticalEdges[x][y]) {
                        buggyPixels[x][y] = true;
                        foundBuggyPixel = true;
                    }
                }
            }
            if (foundBuggyPixel) {
                final LayoutBug layoutBug = createLayoutBug("Detected text near or overlapping vertical edge.", driver, buggyPixels);
                return singleton(layoutBug);
            } else {
                return emptyList();
            }
        } else {
            return emptyList();
        }
    }

}
