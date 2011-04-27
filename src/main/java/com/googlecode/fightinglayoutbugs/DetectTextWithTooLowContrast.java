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

package com.googlecode.fightinglayoutbugs;

import java.util.*;

import static com.googlecode.fightinglayoutbugs.ImageHelper.getContrast;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * @author Michael Tamm
 */
public class DetectTextWithTooLowContrast extends AbstractLayoutBugDetector {

    private class Analyzer {
        private final int[][] screenshot;
        private final int w;
        private final int h;
        private final boolean[][] text;
        private final boolean[][] handled;
        private final int[] minY;
        private final int[] maxY;
        private final int w1;
        private final int h1;
        private int minX;
        private int maxX;

        public boolean foundBuggyPixels;
        public final boolean[][] buggyPixels;

        private Analyzer(WebPage webPage) {
            screenshot = webPage.getScreenshot().pixels;
            w = screenshot.length;
            h = screenshot[0].length;
            text = webPage.getTextPixels();
            handled = new boolean[w][h];
            buggyPixels = new boolean[w][h];
            minY = new int[w];
            maxY = new int[w];
            w1 = w - 1;
            h1 = h - 1;
        }

        private void run() {
            foundBuggyPixels = false;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (text[x][y] && !handled[x][y]) {
                        handleTextArea(x, y);
                    }
                }
            }
        }

        /**
         * @param x0 horizontal coordinate of the starting point of an unhandled text area
         * @param y0 vertical coordinate of the starting point of an unhandled text area
         */
        private void handleTextArea(int x0, int y0) {
            // Determine min and max y for each column as well as min and max x ...
            findAllConnectedTextPixelsStartingFrom(x0, y0);
            // Prevent false alarms because of anti aliasing ...
            if (maxX - minX >= 4) {
                int minNumberOfColumnsWithTooLowContrastBeforeBugIsReported = Math.min(10, (maxX - minX) + 1);
                // Check for too low contrast in each column ...
                int buggyColumns = 0;
                int x = minX;
                do {
                    if (tooLowContrastInColumn(x)) {
                        ++buggyColumns;
                        if (buggyColumns == minNumberOfColumnsWithTooLowContrastBeforeBugIsReported) {
                            foundBuggyPixels = true;
                            // Mark previous columns as well as current column as buggy ...
                            for (int i = (x - minNumberOfColumnsWithTooLowContrastBeforeBugIsReported) + 1; i <= x; ++i) {
                                markTextPixelsAsBuggyInColumn(i);
                            }
                            ++x;
                            while (x <= maxX && tooLowContrastInColumn(x)) {
                                markTextPixelsAsBuggyInColumn(x);
                                ++x;
                            }
                            buggyColumns = 0;
                        } else {
                            ++x;
                        }
                    } else {
                        buggyColumns = 0;
                        ++x;
                    }
                } while (x <= maxX);
            }
        }

        private void findAllConnectedTextPixelsStartingFrom(int x0, int y0) {
            minX = x0;
            maxX = x0;
            for (int x = 0; x < w; ++x) {
                minY[x] = h;
                maxY[x] = -1;
            }
            final Queue<Point> todo = new LinkedList<Point>();
            todo.add(new Point(x0, y0));
            while (!todo.isEmpty()) {
                final Point p = todo.poll();
                final int x = p.x;
                final int y = p.y;
                if (!handled[x][y]) {
                    if (y < minY[x]) {
                        minY[x] = y;
                    }
                    if (y > maxY[x]) {
                        maxY[x] = y;
                    }
                    if (x < minX) {
                        minX = x;
                    }
                    if (x > maxX) {
                        maxX = x;
                    }
                    handled[x][y] = true;
                    // Do we need to visit the pixel above? ...
                    if (y > 0) {
                        final int y1 = y - 1;
                        if (!handled[x][y1] && text[x][y1]){
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Do we need to visit the pixel to the right? ...
                    if (x < w1) {
                        final int x1 = x + 1;
                        if (!handled[x1][y] && text[x1][y]) {
                            todo.add(new Point(x1, y));
                        }
                    }
                    // Do we need to visit the pixel below? ...
                    if (y < h1) {
                        final int y1 = y + 1;
                        if (!handled[x][y1] && text[x][y1]) {
                            todo.add(new Point(x, y1));
                        }
                    }
                    // Do we need to visit the pixel to the left? ...
                    if (x > 0) {
                        final int x1 = x - 1;
                        if (!handled[x1][y] && text[x1][y]){
                            todo.add(new Point(x1, y));
                        }
                    }
                }
            }
        }

        private boolean tooLowContrastInColumn(int x) {
            int y = minY[x];
            int backgroundColor;
            while (true) {
                if (y > 0) {
                    assert !text[x][y - 1] && text[x][y];
                    backgroundColor = screenshot[x][y - 1];
                    // Check contrast to background color above text pixels ...
                    if (getContrast(screenshot[x][y], backgroundColor) >= _minReadableContrast) {
                        return false;
                    }
                    ++y;
                    if (y < h && text[x][y] && getContrast(screenshot[x][y], backgroundColor) >= _minReadableContrast) {
                        return false;
                    }
                }
                // Go to last compound text pixel in current column ...
                while (y < h && text[x][y]) {
                    ++y;
                }
                if (y < h) {
                    assert text[x][y - 1] && !text[x][y];
                    backgroundColor = screenshot[x][y];
                    // Check contrast to background color below text pixels ...
                    if (getContrast(screenshot[x][y - 1], backgroundColor) >= _minReadableContrast) {
                        return false;
                    }
                    if (y >= 2 && text[x][y - 2] && getContrast(screenshot[x][y - 2], backgroundColor) >= _minReadableContrast) {
                        return false;
                    }
                }
                if (y > maxY[x]) {
                    return true;
                }
                // Go to next text pixel in current column ...
                while (!text[x][y]) {
                    ++y;
                }
            }
        }

        private void markTextPixelsAsBuggyInColumn(int x) {
            for (int y = minY[x]; y <= maxY[x]; ++y) {
                if (text[x][y]) {
                    buggyPixels[x][y] = true;
                }
            }
        }
    }

    private double _minReadableContrast = 1.5;

    /**
     * Sets the minimal contrast considered to be readable, default is <code>1&#46;5</code>.
     */
    public void setMinReadableContrast(double minReadableContrast) {
        _minReadableContrast = minReadableContrast;
    }

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) {
        Analyzer analyzer = new Analyzer(webPage);
        analyzer.run();
        if (analyzer.foundBuggyPixels) {
            final LayoutBug layoutBug = createLayoutBug("Detected text with too low contrast.", webPage, new SurroundBuggyPixels(analyzer.buggyPixels));
            return singleton(layoutBug);
        } else {
            return emptyList();
        }
    }
}
