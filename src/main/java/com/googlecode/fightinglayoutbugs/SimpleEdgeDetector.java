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

import static com.googlecode.fightinglayoutbugs.ImageHelper.getContrast;
import static com.googlecode.fightinglayoutbugs.Screenshot.withNoText;
import static com.googlecode.fightinglayoutbugs.StringHelper.amountString;

/**
 * <p>Detects horizontal and vertical edges with a simple algorithm.</p>
 * For horizontal edges:<ol>
 *     <li>candidates = all pixels which have a {@link #setEdgeMinContrast high contrast} to the pixel below/above.
 *     <li>edges = all horizontal pixel sequences in the candidates of {@link #setSimilarColorMaxContrast similar color}
 *         and with a certain {@link #setMinHorizontalEdgeLength minimal length}
 * </ol>
 * For horizontal edges:<ol>
 *     <li>candidates = all pixels which have a {@link #setEdgeMinContrast high contrast} to the pixel on the left/right.
 *     <li>edges = all vertical pixel sequences in the candidates of {@link #setSimilarColorMaxContrast similar color}
 *         and with a certain {@link #setMinVerticalEdgeLength minimal length}
 * </ol>
 *
 * @author Michael Tamm
 */
public class SimpleEdgeDetector implements EdgeDetector {

    private double _edgeMinContrast = 1.5;
    private double _similarColorMaxContrast = 1.5;
    private int _minHorizontalEdgeLength = 16;
    private int _minVerticalEdgeLength = 10;

    /**
     * Sets the minimal contrast two pixels must have to be considered as candidates for an edge, default is <code>1&#46;75</code>
     * -- see <a href="http://trace.wisc.edu/contrast-ratio-examples/TextSamples_6x6x6_On_000000.htm">Web-safe Colored Text shown on #000</a>
     * for a visualization of different contrast values.
     */
    public void setEdgeMinContrast(double edgeMinContrast) {
        _edgeMinContrast = edgeMinContrast;
    }

    public double getEdgeMinContrast() {
        return _edgeMinContrast;
    }

    /**
     * Sets the maximal contrast two pixels can have to be considered as having similar colors, default is <code>1&#46;5</code>
     * -- see <a href="http://trace.wisc.edu/contrast-ratio-examples/TextSamples_6x6x6_On_000000.htm">Web-safe Colored Text shown on #000</a>
     * for a visualization of different contrast values.
     */
    public void setSimilarColorMaxContrast(double similarColorMaxContrast) {
        _similarColorMaxContrast = similarColorMaxContrast;
    }

    public double getSimilarColorMaxContrast() {
        return _similarColorMaxContrast;
    }

    /**
     * Sets the minimal length for detected horizontal edges, default is <code>16</code>.
     */
    public void setMinHorizontalEdgeLength(int minHorizontalEdgeLength) {
        _minHorizontalEdgeLength = minHorizontalEdgeLength;
    }

    public int getMinHorizontalEdgeLength() {
        return _minHorizontalEdgeLength;
    }

    /**
     * Sets the minimal length for detected vertical edges, default is <code>10</code>.
     */
    public void setMinVerticalEdgeLength(int minVerticalEdgeLength) {
        _minVerticalEdgeLength = minVerticalEdgeLength;
    }

    public int getMinVerticalEdgeLength() {
        return _minVerticalEdgeLength;
    }

    public boolean[][] detectHorizontalEdgesIn(WebPage webPage) {
        final int[][] pixels = webPage.getScreenshot(withNoText()).pixels;
        final int w = pixels.length;
        final int h = pixels[0].length;
        // 1.) Determine candidates (those pixels, which have a high contrast to the pixel below/above itself) ...
        final int h1 = h - 1;
        boolean[][] candidates = new boolean[w][h];
        for (int y = 0; y < h1; ++y) {
            for (int x = 0; x < w; ++x) {
                if (haveHighContrast(pixels[x][y], pixels[x][y + 1])) {
                    candidates[x][y] = true;
                    candidates[x][y + 1] = true;
                }
            }
        }
        Visualization.algorithmStepFinished("1.) Determine candidates (those pixels, which have a high contrast to the pixel below/above itself).", candidates);
        // 2.) Find horizontal pixels sequences in candidates of similar color with configured minimal length ...
        boolean[][] horizontalEdges = new boolean[w][h];
        for (int y = 0; y < h; ++y) {
            int x1 = 0;
            do {
                // find first candidate pixel ...
                while (x1 < w && !candidates[x1][y]) {
                    ++x1;
                }
                if (x1 < w) {
                    // find end of horizontal edge ...
                    int p1 = pixels[x1][y];
                    int x2 = x1 + 1;
                    int p2;
                    while (x2 < w && candidates[x2][y] && haveSimilarColor(p1, p2 = pixels[x2][y])) {
                        p1 = p2;
                        ++x2;
                    }
                    if (x2 - x1 >= _minHorizontalEdgeLength) {
                        for (int x = x1; x < x2; ++x) {
                            horizontalEdges[x][y] = true;
                        }
                    }
                    x1 = x2;
                }
            } while(x1 < w);
        }
        Visualization.algorithmFinished("2.) Done: Find horizontal pixels sequences in candidates of similar color with minimal " + amountString(_minHorizontalEdgeLength, "pixel") + " length.", horizontalEdges);
        return horizontalEdges;
    }

    public boolean[][] detectVerticalEdgesIn(WebPage webPage) {
        final int[][] pixels = webPage.getScreenshot(withNoText()).pixels;
        final int w = pixels.length;
        final int h = pixels[0].length;
        // 1.) Determine candidates (those pixels, which have a high contrast to the pixel on the left/right) ...
        final int w1 = w - 1;
        boolean[][] candidates = new boolean[w][h];
        for (int x = 0; x < w1; ++x) {
            for (int y = 0; y < h; ++y) {
                if (haveHighContrast(pixels[x][y], pixels[x + 1][y])) {
                    candidates[x][y] = true;
                    candidates[x + 1][1] = true;
                }
            }
        }
        Visualization.algorithmStepFinished("1.) Determine candidates (those pixels, which have a high contrast to the pixel on the left/right).", candidates);
        // 2.) Find vertical pixels sequences in candidates of similar color and with configured minimal length ...
        boolean[][] verticalEdges = new boolean[w][h];
        for (int x = 0; x < w; ++x) {
            int y1 = 0;
            do {
                // find first candidate pixel ...
                while (y1 < h && !candidates[x][y1]) {
                    ++y1;
                }
                if (y1 < h) {
                    // find end of vertical edge ...
                    int p1 = pixels[x][y1];
                    int y2 = y1 + 1;
                    int p2;
                    while (y2 < h && candidates[x][y2] && haveSimilarColor(p1, p2 = pixels[x][y2])) {
                        p1 = p2;
                        ++y2;
                    }
                    if (y2 - y1 >= _minVerticalEdgeLength) {
                        for (int y = y1; y < y2; ++y) {
                            verticalEdges[x][y] = true;
                        }
                    }
                    y1 = y2;
                }
            } while(y1 < h);
        }
        Visualization.algorithmFinished("2.) Done: Find vertical pixels sequences in candidates of similar color and with minimal " + amountString(_minVerticalEdgeLength, "pixel") + " length.", verticalEdges);
        return verticalEdges;
    }

    private boolean haveHighContrast(int rgb1, int rgb2) {
        return (rgb1 != rgb2) && (getContrast(rgb1, rgb2) >= _edgeMinContrast);
    }

    private boolean haveSimilarColor(int rgb1, int rgb2) {
        return (rgb1 == rgb2) || (getContrast(rgb1, rgb2) <= _similarColorMaxContrast);
    }

}
