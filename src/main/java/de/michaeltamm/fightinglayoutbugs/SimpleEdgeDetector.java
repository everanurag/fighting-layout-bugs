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

/**
 * @author Michael Tamm
 */
public class SimpleEdgeDetector implements EdgeDetector {

    private static final int SIMILAR_COLORS_MAX_RGB_SQR_DISTANCE = 30;
    private static final int EDGE_MIN_RGB_SQR_DISTANCE = 900;

    public boolean[][] detectVerticalEdgesIn(WebPage webPage, int minLength) throws Exception {
        // Take screenshot ...
        final int[][] pixels = webPage.getScreenshotWithoutText();
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
                if (y == h || sqrDistance(r, g, b, (c = pixels[x][y])) > SIMILAR_COLORS_MAX_RGB_SQR_DISTANCE) {
                    if (y - start >= minLength) {
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
        final boolean[][] result = new boolean[w][h];
        // Detect vertical edges ...
        final int w1 = w - 1;
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (candidates[x][y]) {
                    if (x > 0 && sqrDistance(pixels[x - 1][y], pixels[x][y]) > EDGE_MIN_RGB_SQR_DISTANCE) {
                        result[x - 1][y] = true;
                        result[x][y] = true;
                    }
                    if (x < w1 && sqrDistance(pixels[x][y], pixels[x + 1][y]) > EDGE_MIN_RGB_SQR_DISTANCE) {
                        result[x][y] = true;
                        result[x + 1][y] = true;
                    }
                }
            }
        }
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
                    if (y2 - y1 < minLength) {
                        for (int i = y1; i < y2; ++i) {
                            result[x][i] = false;
                        }
                    }
                    y1 = y2;
                }
            } while(y1 < h);
        }
        return result;
    }

    public boolean[][] detectHorizontalEdgesIn(WebPage webPage, int minLength) throws Exception {
        final int[][] pixels = webPage.getScreenshotWithoutText();
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
                if (x == w || sqrDistance(r, g, b, (c = pixels[x][y])) > SIMILAR_COLORS_MAX_RGB_SQR_DISTANCE) {
                    if (x - start >= minLength) {
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
        final boolean[][] result = new boolean[w][h];
        // Detect horizontal edges ...
        final int h1 = h - 1;
        for (int y = 0; y < h; ++y) {
            for (int x = 0; x < w; ++x) {
                if (candidates[x][y]) {
                    if (y > 0 && sqrDistance(pixels[x][y - 1], pixels[x][y]) > EDGE_MIN_RGB_SQR_DISTANCE) {
                        result[x][y - 1] = true;
                        result[x][y] = true;
                    }
                    if (y < h1 && sqrDistance(pixels[x][y], pixels[x][y + 1]) > EDGE_MIN_RGB_SQR_DISTANCE) {
                        result[x][y] = true;
                        result[x][y + 1] = true;
                    }
                }
            }
        }
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
                    if (x2 - x1 < minLength) {
                        for (int i = x1; i < x2; ++i) {
                            result[i][y] = false;
                        }
                    }
                    x1 = x2;
                }
            } while(x1 < w);
        }
        return result;
    }

    private static int sqrDistance(int rgb1, int rgb2) {
        int r = (rgb1 & 0xFF0000) >> 16;
        int g = (rgb1 & 0xFF00) >> 8;
        int b = (rgb1 & 0xFF);
        r -= (rgb2 & 0xFF0000) >> 16;
        g -= (rgb2 & 0xFF00) >> 8;
        b -= (rgb2 & 0xFF);
        return r * r + g * g + b * b;
    }

    private static int sqrDistance(int r1, int g1, int b1, int rgb2) {
        final int dr = r1 - ((rgb2 & 0xFF0000) >> 16);
        final int dg = g1 - ((rgb2 & 0xFF00) >> 8);
        final int db = b1 - ((rgb2 & 0xFF));
        return dr * dr + dg * dg + db * db;
    }

}
