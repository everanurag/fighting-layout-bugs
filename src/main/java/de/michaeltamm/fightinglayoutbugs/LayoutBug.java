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

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Represents a layout bug found by a {@link LayoutBugDetector}.
 *
 * @author Michael Tamm
 */
public class LayoutBug {

    private static final int[] CIRCLE = new int[] { 2, 3, 4, 5, 5, 5, 5, 5, 4, 3, 2 };
    private static final int RED = 0x00FF0000;
    private static final int TRANSPARENT = 0xFF000000;

    private final String _description;
    private File _screenshot;

    public LayoutBug(String description, File screenshot) {
        _description = description;
        _screenshot = screenshot;
    }

    public String getDescription() {
        return _description;
    }

    public File getScreenshot() {
        return _screenshot;
    }

    /**
     * Marks the buggy pixels in the screenshot of this layout bug.
     */
    public void markBuggyPixels(boolean[][] buggyPixels) throws IOException {
        if (_screenshot != null) {
            final int w = buggyPixels.length;
            final int h = buggyPixels[0].length;
            final int[][] pixels = ImageHelper.fileToPixels(_screenshot);
            assert pixels.length == w;
            assert pixels[0].length == h;
            // 1.) Find buggy areas by drawing a circle with a radius of 5 pixels around each buggy pixel ...
            final boolean[][] buggyAreas = new boolean[w][h];
            for (int x = 0; x < w; ++x) {
                for (int y = 0; y < h; ++y) {
                    if (buggyPixels[x][y]) {
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
            // 2.) Encircle buggy areas with red lines ...
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
            ImageHelper.blend(pixels, redLines);
            // Sometimes overwriting the existing screenshot filedid not work
            // on my machine (Windows Vista 64 bit, Sun JDK 1.6.0_10 64 bit),
            // therefore I always create a new file here ...
            final String name = _screenshot.getName();
            final String prefix = name.substring(name.indexOf('.') + 1);
            final File newFile = File.createTempFile(prefix, ".png", _screenshot.getParentFile());
            ImageIO.write(ImageHelper.pixelsToImage(pixels), "png", newFile);
            try {
                if (!_screenshot.delete()) {
                    _screenshot.deleteOnExit();
                }
            } finally {
                _screenshot = newFile;
            }
        }
    }

    @Override
    public String toString() {
        if (_screenshot == null) {
            return _description;
        } else {
            try {
                return _description + " - " + _screenshot.getCanonicalPath();
            } catch (IOException ignored) {
                return _description + " - " + _screenshot;
            }
        }
    }
}
