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

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        try {
            webPage.resizeBrowserWindowTo(_minimalSupportedScreenResolution);
        } catch (Exception e) {
            System.err.println("Skipping " + getClass().getSimpleName() + " -- " + e.getMessage());
            return emptyList();
        }
        final int scrollMaxX = ((Number) webPage.executeJavaScript("if (typeof window.scrollMaxX != 'undefined') { return window.scrollMaxX; } else { var x = (document.documentElement ? document.documentElement : document.body); return x.scrollWidth - x.clientWidth; }")).intValue();
        if (scrollMaxX == 0) {
            return emptyList();
        } else {
            final int scrollWidth = ((Number) webPage.executeJavaScript("var x = (document.documentElement ? document.documentElement : document.body); return x.scrollWidth;")).intValue();
            final int scrollHeight = ((Number) webPage.executeJavaScript("var x = (document.documentElement ? document.documentElement : document.body); return x.scrollHeight;")).intValue();
            LayoutBug layoutBug = createLayoutBug("Detected horizontal scroll bar when browser window has size " + _minimalSupportedScreenResolution + ".", webPage, new Marker() {
                public void mark(int[][] screenshot) {
                    int w = screenshot.length;
                    int h = screenshot[0].length;
                    if (w != scrollWidth || h != scrollHeight) {
                        // the screenshot dimension are not the dimensions of the entire web page,
                        // the screenshot probably contains the horizontal scroll bar, mark it ...
                        markHorizontalScrollBar(screenshot);
                    } else {
                        // mark all pixels at the right side, which are responsible
                        // for the horizontal scroll bar ...
                        markPixelsAtRightSide(screenshot, scrollMaxX);
                    }
                }
            });
            return singleton(layoutBug);
        }
    }

    private void markHorizontalScrollBar(int[][] screenshot) {
        int w = screenshot.length;
        int h = screenshot[0].length;
        // Mark horizontal scroll bar ...
        for (int y = h - 20; y < h; ++y) {
            for (int x = y % 2; x < w - 20; x += 2) {
                screenshot[x][y] = 0xFF0000;
            }
        }
        // Fade out irrelevant pixels ...
        int n = 50;
        int[][] fadeOutMask = new int[w][h];
        int y = 0;
        while (y < h - (n + 20)) {
            for (int x = 0; x < w; ++x) {
                fadeOutMask[x][y] = 0x80FFFFFF;
            }
            ++y;
        }
        int[] a = new int[n];
        for (int i = 0; i < n; ++i) {
            a[i] = 128 + (int) Math.round((1 - Math.cos((i * Math.PI) / n)) * 63.5);
        }
        while (y < h - 20) {
            int m = (a[y - (h - (n + 20))] << 24) | 0xFFFFFF;
            for (int x = 0; x < w; ++x) {
                fadeOutMask[x][y] = m;
            }
            ++y;
        }
        while (y < h) {
            for (int x = 0; x < w; ++x) {
                fadeOutMask[x][y] = 0xFFFFFFFF;
            }
            ++y;
        }
        ImageHelper.blend(screenshot, fadeOutMask);
    }

    private void markPixelsAtRightSide(int[][] screenshot, int scrollMaxX) {
        int w = screenshot.length;
        int h = screenshot[0].length;
        // Mark pixels at right side ...
        int maxWidth = w - scrollMaxX;
        for (int x = maxWidth; x < w; ++x) {
            for (int y = (x - maxWidth) % 2; y < h; y += 2) {
                screenshot[x][y] = 0xFF0000;
            }
        }
        // Fade out irrelevant pixels ...
        int n = 50;
        int[][] fadeOutMask = new int[w][h];
        int x = 0;
        while (x < w - (n + scrollMaxX)) {
            for (int y = 0; y < h; ++y) {
                fadeOutMask[x][y] = 0x80FFFFFF;
            }
            ++x;
        }
        int[] a = new int[n];
        for (int i = 0; i < n; ++i) {
            a[i] = 128 + (int) Math.round((1 - Math.cos((i * Math.PI) / n)) * 63.5);
        }
        while (x < w - scrollMaxX) {
            int m = (a[x - (w - (n + scrollMaxX))] << 24) | 0xFFFFFF;
            for (int y = 0; y < h; ++y) {
                fadeOutMask[x][y] = m;
            }
            ++x;
        }
        while (x < w) {
            for (int y = 0; y < h; ++y) {
                fadeOutMask[x][y] = 0xFFFFFFFF;
            }
            ++x;
        }
        ImageHelper.blend(screenshot, fadeOutMask);
    }
}
