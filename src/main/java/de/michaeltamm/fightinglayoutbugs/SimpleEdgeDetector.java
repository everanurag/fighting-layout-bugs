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

import static de.michaeltamm.fightinglayoutbugs.Screenshot.withNoText;

/**
 * <p>Detects horizontal and vertical edges with a simple algorithm.</p>
 * For horizontal edges:<ol>
 *     <li>candidates = horizontal pixel sequences of the same or {@link #setSimilarColorsMaxRgbSqrDistance similar colors} with a certain {@link #setMinEdgeLength minimal length}</li>
 *     <li>Discard all candidate pixels, which don't have a {@link #setEdgeMinRgbSqrDistance high contrast} to the line above or below</li>
 *     <li>Discard all remaining pixel sequences, which don't have a certain {@link #setMinEdgeLength minimal length}
 * </ol>
 * For vertical edges:<ol>
 *     <li>candidates = vertical pixel sequences of the same or {@link #setSimilarColorsMaxRgbSqrDistance similar colors} with a certain {@link #setMinEdgeLength minimal length}</li>
 *     <li>Discard all candidate pixels, which don't have a {@link #setEdgeMinRgbSqrDistance high contrast} to the column at the left or at the right</li>
 *     <li>Discard all remaining pixel sequences, which don't have a certain {@link #setMinEdgeLength minimal length}
 * </ol>
 *
 * @author Michael Tamm
 */
public class SimpleEdgeDetector implements EdgeDetector {

    private int _minEdgeLength = 16;
    private int _similarColorsMaxRgbSqrDistance = 30;
    private int _edgeMinRgbSqrDistance = 900;

    /**
     * Sets the minimal length for detected edges in pixels, default is <code>16</code>.
     */
    public void setMinEdgeLength(int length) {
        _minEdgeLength = length;
    }

    /**
     * Sets the maximal {@link #getRgbSqrDistance distance} for two colors to be considered similar, default is 30.
     */
    public void setSimilarColorsMaxRgbSqrDistance(int rgbSqrDistance) {
        _similarColorsMaxRgbSqrDistance = rgbSqrDistance;
    }

    /**
     * Sets the minimal {@link #getRgbSqrDistance distance} for two colors to be considered of having such a high contrast,
     * that they might belong to an edge, default is 900.
     */
    public void setEdgeMinRgbSqrDistance(int rgbSqrDistance) {
        _edgeMinRgbSqrDistance = rgbSqrDistance;
    }

    public boolean[][] detectHorizontalEdgesIn(WebPage webPage) {
        final int[][] pixels = webPage.getScreenshot(withNoText()).pixels;
        final int w = pixels.length;
        final int h = pixels[0].length;
        // Detect horizontal pixel sequences of similar color ...
        final boolean[][] candidates = new boolean[w][h];
        for (int y = 0; y < h; ++y) {
            int start = 0;
            int c = pixels[0][y];
            int r = (c & 0xFF0000) >> 16;
            int g = (c & 0xFF00) >> 8;
            int b = (c & 0xFF);
            for (int x = 1; x <= w; ++x) {
                if (x == w || getRgbSqrDistance(r, g, b, (c = pixels[x][y])) > _similarColorsMaxRgbSqrDistance) {
                    if (x - start >= _minEdgeLength) {
                        // All pixels from pixels[start][y] to pixels[x - 1][y] have similar color ...
                        for (int i = start; i < x; ++i) {
                            candidates[i][y] = true;
                        }
                    }
                    if (x < w) {
                        start = x;
                        r = (c & 0xFF0000) >> 16;
                        g = (c & 0xFF00) >> 8;
                        b = (c & 0xFF);
                    }
                } else {
                    r = (c & 0xFF0000) >> 16;
                    g = (c & 0xFF00) >> 8;
                    b = (c & 0xFF);
                }
            }
        }
        Visualization.algorithmStepFinished("1.) candidates = horizontal pixel sequences of similar color", candidates);
        final boolean[][] result = new boolean[w][h];
        // Detect horizontal edges by comparing candidates with pixel above and below ...
        final int h1 = h - 1;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                if (candidates[x][y]) {
                    if (y > 0 && getRgbSqrDistance(pixels[x][y - 1], pixels[x][y]) > _edgeMinRgbSqrDistance) {
                        result[x][y - 1] = true;
                        result[x][y] = true;
                    }
                    if (y < h1 && getRgbSqrDistance(pixels[x][y], pixels[x][y + 1]) > _edgeMinRgbSqrDistance) {
                        result[x][y] = true;
                        result[x][y + 1] = true;
                    }
                }
            }
        }
        Visualization.algorithmStepFinished("2.) Detect horizontal edges by comparing candidates with pixels above and below", result);
        // Ignore detected horizontal edges which are not long enough ...
        for (int y = 0; y < h; ++y) {
            int x1 = 0;
            do {
                while (x1 < w && !result[x1][y]) {
                    ++x1;
                }
                if (x1 < w) {
                    int x2 = x1 + 1;
                    while (x2 < w && result[x2][y]) {
                        ++x2;
                    }
                    if (x2 - x1 < _minEdgeLength) {
                        for (int i = x1; i < x2; ++i) {
                            result[i][y] = false;
                        }
                    }
                    x1 = x2;
                }
            } while(x1 < w);
        }
        Visualization.algorithmFinished("3.) Ignore detected horizontal edges which are not long enough", result);
        return result;
    }

    public boolean[][] detectVerticalEdgesIn(WebPage webPage) {
        // Take screenshot ...
        final int[][] pixels = webPage.getScreenshot(withNoText()).pixels;
        final int w = pixels.length;
        final int h = pixels[0].length;
        // Detect vertical pixel sequences of similar color ...
        final boolean[][] candidates = new boolean[w][h];
        for (int x = 0; x < w; ++x) {
            int start = 0;
            int c = pixels[x][0];
            int r = (c & 0xFF0000) >> 16;
            int g = (c & 0xFF00) >> 8;
            int b = (c & 0xFF);
            for (int y = 1; y <= h; ++y) {
                if (y == h || getRgbSqrDistance(r, g, b, (c = pixels[x][y])) > _similarColorsMaxRgbSqrDistance) {
                    if (y - start >= _minEdgeLength) {
                        // All pixels from pixels[x][start] to pixels[x][y - 1] have similar color ...
                        for (int i = start; i < y; ++i) {
                            candidates[x][i] = true;
                        }
                    }
                    if (y < h) {
                        start = y;
                        r = (c & 0xFF0000) >> 16;
                        g = (c & 0xFF00) >> 8;
                        b = (c & 0xFF);
                    }
                } else {
                    r = (c & 0xFF0000) >> 16;
                    g = (c & 0xFF00) >> 8;
                    b = (c & 0xFF);
                }
            }
        }
        Visualization.algorithmStepFinished("1.) candidates = vertical pixel sequences of similar color", candidates);
        final boolean[][] result = new boolean[w][h];
        // Detect vertical edges ...
        final int w1 = w - 1;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (candidates[x][y]) {
                    if (x > 0 && getRgbSqrDistance(pixels[x - 1][y], pixels[x][y]) > _edgeMinRgbSqrDistance) {
                        result[x - 1][y] = true;
                        result[x][y] = true;
                    }
                    if (x < w1 && getRgbSqrDistance(pixels[x][y], pixels[x + 1][y]) > _edgeMinRgbSqrDistance) {
                        result[x][y] = true;
                        result[x + 1][y] = true;
                    }
                }
            }
        }
        Visualization.algorithmStepFinished("2.) Detect vertical edges by comparing candidates with pixels on left and right side", result);
        // Ignore detected vertical edges which are not long enough ...
        for (int x = 0; x < w; ++x) {
            int y1 = 0;
            do {
                while (y1 < h && !result[x][y1]) {
                    ++y1;
                }
                if (y1 < h) {
                    int y2 = y1 + 1;
                    while (y2 < h && result[x][y2]) {
                        ++y2;
                    }
                    if (y2 - y1 < _minEdgeLength) {
                        for (int i = y1; i < y2; ++i) {
                            result[x][i] = false;
                        }
                    }
                    y1 = y2;
                }
            } while(y1 < h);
        }
        Visualization.algorithmFinished("3.) Ignore detected vertical edges which are not long enough", result);
        return result;
    }

    /**
     * Calculates the squared distance between the colors of two pixels in the RGB cube.
     * The exact formula is: <code>dr * dr + dg * dg + db * db</code> whereby <code>dr</code>
     * is the delta of red color component of the two given pixels (in the range 0 ... 255),
     * <code>dg</code> the delta of the green color component and <code>db</code> the delta
     * of the blue color component.
     */
    public static int getRgbSqrDistance(int pixel1, int pixel2) {
        int r = (pixel1 & 0xFF0000) >> 16;
        int g = (pixel1 & 0xFF00) >> 8;
        int b = (pixel1 & 0xFF);
        r -= (pixel2 & 0xFF0000) >> 16;
        g -= (pixel2 & 0xFF00) >> 8;
        b -= (pixel2 & 0xFF);
        return r * r + g * g + b * b;
    }

    private static int getRgbSqrDistance(int r1, int g1, int b1, int pixel2) {
        final int dr = r1 - ((pixel2 & 0xFF0000) >> 16);
        final int dg = g1 - ((pixel2 & 0xFF00) >> 8);
        final int db = b1 - ((pixel2 & 0xFF));
        return dr * dr + dg * dg + db * db;
    }
}
