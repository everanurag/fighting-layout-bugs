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
 * Marks buggy pixels in the screenshot by
 * surrounding them with thick red lines.
 *
 * @author Michael Tamm
 */
public class SurroundBuggyPixels implements Marker {

    private static final int[] CIRCLE = new int[] { 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2 };
    private static final int RED = 0x00FF0000;
    private static final int TRANSPARENT = 0xFF000000;

    private final boolean[][] _buggyPixels;

    public SurroundBuggyPixels(boolean[][] buggyPixels) {
        _buggyPixels = buggyPixels;
    }

    public void mark(int[][] screenshot) {
        final int w = _buggyPixels.length;
        final int h = _buggyPixels[0].length;
        if (screenshot.length != w || screenshot[0].length != h) {
            throw new RuntimeException("Buggy pixels (" + w + " x " + h + ") have different size than screenshot (" + screenshot.length + " x " + screenshot[0].length + ").");
        }
        // 1.) Find buggy areas by drawing a circle with a radius of 5 pixels around each buggy pixel ...
        final boolean[][] buggyAreas = new boolean[w][h];
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (_buggyPixels[x][y]) {
                    for (int i = -5; i <= 5; ++i) {
                        final int yy = y + i;
                        if (y >= 0 && y < h) {
                            final int n = CIRCLE[i + 5];
                            for (int j = -n; j < n; ++j) {
                                final int xx = x + j;
                                if (xx >= 0 && xx < w) {
                                    buggyAreas[xx][yy] = true;
                                }
                            }
                        }
                    }
                }
            }
        }
        // 2.) Surround buggy areas with red lines ...
        final int[][] redLines = new int[w][h];
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                redLines[x][y] = TRANSPARENT;
            }
        }
        final boolean[][] buggyAreasOutlines = ImageHelper.findOutlines(buggyAreas);
        for (int x = 0; x < w; ++x) {
            for (int y = 0; y < h; ++y) {
                if (buggyAreasOutlines[x][y]) {
                    for (int i = -1; i <= 1; ++i) {
                        final int xx = x + i;
                        if (xx >= 0 && xx < w) {
                            for (int j = -1; j <= 1; ++j) {
                                final int yy = y + j;
                                if (yy >= 0 && yy < h) {
                                    redLines[xx][yy] = RED;
                                }
                            }
                        }
                    }
                }
            }
        }
        // 3.) Blend red lines into screenshot ...
        ImageHelper.blend(screenshot, redLines);
    }
}
