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

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * Detects if a web page needs horizontal scrolling. You might want to configure the
 * {@link #setMinimalSupportedScreenResolution minimal supported screen resolution}
 * before using this detector, default is 1024 x 768.
 *
 * @author Michael Tamm
 */
public class DetectNeedsHorizontalScrolling extends AbstractLayoutBugDetector {

    private Dimension _minimalSupportedScreenResolution = new Dimension(1024, 768);

    /**
     * Sets the minimal supported screen resolution, default is 1024 x 768.
     */
    public void setMinimalSupportedScreenResolution(int width, int height) {
        _minimalSupportedScreenResolution = new Dimension(width, height);
    }

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) throws Exception {
        webPage.resizeBrowserWindowTo(_minimalSupportedScreenResolution);
        final int scrollMaxX = ((Number) webPage.executeJavaScript("return window.scrollMaxX")).intValue();
        if (scrollMaxX > 0) {
            LayoutBug layoutBug = createLayoutBug("Detected horizontal scroll bar when browser window has size " + _minimalSupportedScreenResolution + ".", webPage, new Marker() {
                public void mark(int[][] screenshot) {
                    int w = screenshot.length;
                    int h = screenshot[0].length;
                    int maxWidth = w - scrollMaxX;
                    for (int x = maxWidth; x < w; ++x) {
                        for (int y = (x - maxWidth) % 2; y < h; y += 2) {
                            screenshot[x][y] = 0xFF0000;
                        }
                    }

                }
            });
            return singleton(layoutBug);
        } else {
            return emptyList();
        }
    }
}
