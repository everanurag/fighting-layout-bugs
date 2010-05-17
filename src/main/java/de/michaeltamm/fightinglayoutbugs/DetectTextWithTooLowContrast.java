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

import java.util.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.singleton;

/**
 * @author Michael Tamm
 */
public class DetectTextWithTooLowContrast extends AbstractLayoutBugDetector {

    private double _minReadableContrast = 1.5;

    /**
     * Sets the minimal contrast considered to be readable, default is <code>1&#46;5</code>.
     */
    public void setMinReadableContrast(double minReadableContrast) {
        _minReadableContrast = minReadableContrast;
    }

    public Collection<LayoutBug> findLayoutBugsIn(WebPage webPage) throws Exception {
        final int[][] screenshot = webPage.getScreenshot();
        final int w = screenshot.length;
        final int h = screenshot[0].length;
        if (w > 0 && h > 0) {
            final boolean[][] text = webPage.getTextPixels();
            final boolean[][] handled = new boolean[w][h];
            final boolean[][] buggyPixels = new boolean[w][h];
            boolean foundBuggyPixel = false;
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (text[x][y] && !handled[x][y]) {
                        foundBuggyPixel |= handleTextArea(x, y, screenshot, text, handled, buggyPixels);
                    }
                }
            }
            if (foundBuggyPixel) {
                final LayoutBug layoutBug = createLayoutBug("Detected text with too low contrast.", webPage, new SurroundBuggyPixels(buggyPixels));
                return singleton(layoutBug);
            } else {
                return emptyList();
            }
        } else {
            return emptyList();
        }
    }

    /**
     * Returns <code>true</code> if buggy pixels are found.
     * @param x0 horizontal coordinate of the starting point of an unhandled text area
     * @param y0 vertical coordinate of the starting point of an unhandled text area
     */
    private boolean handleTextArea(int x0, int y0, int[][] screenshot, boolean[][] text, boolean[][] handled, boolean[][] buggyPixels) {
        boolean foundBuggyPixel = false;
        final int w = screenshot.length;
        final int h = screenshot[0].length;
        final int[] minY = new int[w];
        final int[] maxY = new int[w];
        for (int x = 0; x < w; ++x) {
            minY[x] = h;
            maxY[x] = -1;
        }
        final int w1 = w - 1;
        final int h1 = h - 1;
        // 1.) Visit all pixels in the text area and determine min and max y for each column ...
        final Queue<Point> todo = new LinkedList<Point>();
        todo.add(new Point(x0, y0));
        final boolean[][] visited = new boolean[w][h];
        while (!todo.isEmpty()) {
            final Point p = todo.poll();
            final int x = p.x;
            final int y = p.y;
            if (!visited[x][y]) {
                // Handle p ...
                if (y < minY[x]) {
                    minY[x] = y;
                }
                if (y > maxY[x]) {
                    maxY[x] = y;
                }
                visited[x][y] = true;
                handled[x][y] = true;
                // Do we need to visit the pixel above? ...
                if (y > 0) {
                    final int y1 = y - 1;
                    if (!visited[x][y1] && text[x][y1]){
                        todo.add(new Point(x, y1));
                    }
                }
                // Do we need to visit the pixel to the right? ...
                if (x < w1) {
                    final int x1 = x + 1;
                    if (!visited[x1][y] && text[x1][y]) {
                        todo.add(new Point(x1, y));
                    }
                }
                // Do we need to visit the pixel below? ...
                if (y < h1) {
                    final int y1 = y + 1;
                    if (!visited[x][y1] && text[x][y1]) {
                        todo.add(new Point(x, y1));
                    }
                }
                // Do we need to visit the pixel to the left? ...
                if (x > 0) {
                    final int x1 = x - 1;
                    if (!visited[x1][y] && text[x1][y]){
                        todo.add(new Point(x1, y));
                    }
                }
            }
        }
        // 2.) Check for too low contrast in each column ...
        final int w2 = w - 2;
        for (int x = 2; x < w2; ++x) {
            final int y1 = minY[x];
            final int y2 = maxY[x];
            if ((y1 < h && y1 > 0) || (y2 > -1 && y2 < h1)) {
                final Integer backgroundColorAbove = (y1 > 0 ? screenshot[x][y1 - 1] : null);
                final Integer backgroundColorBelow = (y2 < h1 ? screenshot[x][y2 + 1] : null);
                final Set<Integer> colorsOfNearTextPixels = new HashSet<Integer>();
                for (int i = -2; i <= 2; ++i) {
                    final int xx = x + i;
                    for (int y = y1; y <= y2; ++y) {
                        if (visited[xx][y]) {
                            colorsOfNearTextPixels.add(screenshot[xx][y]);
                        }
                    }
                }
                // The text has too low contrast in the current column x,
                // if all of the near text pixels have too low contrast
                // compared to backgroundColorAbove and backgroundColorBelow ...
                boolean tooLowContrastInCurrentColumn = true;
                if (backgroundColorAbove != null && backgroundColorBelow != null && backgroundColorAbove.intValue() == backgroundColorBelow.intValue()) {
                    @SuppressWarnings({"UnnecessaryLocalVariable"})
                    final int backgroundColor = backgroundColorAbove;
                    for (final Integer c : colorsOfNearTextPixels) {
                        if (!tooLowContrast(backgroundColor, c)) {
                            tooLowContrastInCurrentColumn = false;
                            break;
                        }
                    }
                } else {
                    for (final Integer c : colorsOfNearTextPixels) {
                        if ((backgroundColorAbove != null && !tooLowContrast(backgroundColorAbove, c)) || (backgroundColorBelow != null && !tooLowContrast(backgroundColorBelow, c))) {
                            tooLowContrastInCurrentColumn = false;
                            break;
                        }
                    }
                }
                if (tooLowContrastInCurrentColumn) {
                    foundBuggyPixel = true;
                    // mark all pixels in the current column x which belong to
                    // the currently analyzed text area as buggy ...
                    for (int y = y1; y <= y2; ++y) {
                        if (visited[x][y]) {
                            buggyPixels[x][y] = true;
                        }
                    }
                }
            }
        }
        return foundBuggyPixel;
    }

    private boolean tooLowContrast(int rgb1, int rgb2) {
        return getContrast(rgb1, rgb2) < _minReadableContrast;
    }

    /**
     * Determines the contrast between the two given colors
     * based on the <a href="http://www.w3.org/TR/WCAG20-TECHS/G17.html#G17-procedure">WCAG 2.0 formula</a>.
     */
    private static double getContrast(int rgb1, int rgb2) {
        double l1 = getLuminance(rgb1);
        double l2 = getLuminance(rgb2);
        return ((l1 >= l2) ? (l1 + 0.05) / (l2 + 0.05) : (l2 + 0.05) / (l1 + 0.05));
    }

    private static double getLuminance(int rgb) {
        double r = ((rgb & 0xFF0000) >> 16) / 255.0;
        r = (r <= 0.03928 ? r / 12.92 : Math.pow((r + 0.055)/1.055, 2.4));
        double g = ((rgb & 0xFF00) >> 8) / 255.0;
        g = (g <= 0.03928 ? g / 12.92 : Math.pow((g + 0.055)/1.055, 2.4));
        double b = (rgb & 0xFF) / 255.0;
        b = (b <= 0.03928 ? b / 12.92 : Math.pow((b + 0.055)/1.055, 2.4));
        return 0.2126 * r + 0.7152 * g + 0.0722 * b;
    }
}
