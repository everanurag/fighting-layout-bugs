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

/**
 * @author Michael Tamm
 */
public class CompareScreenshots {

    public final int width;
    public final int height;
    public final boolean[][] differentPixels;

    public boolean noDifferencesFound;

    public CompareScreenshots(Screenshot screenshot1, Screenshot screenshot2) {
        if (!screenshot1.dimension.equals(screenshot2.dimension)) {
            throw new IllegalArgumentException("Given screenshot1 has dimension " + screenshot1.dimension + " but screenshot2 has dimension " + screenshot2.dimension + ".");
        }
        int[][] pixels1 = screenshot1.pixels;
        int[][] pixels2 = screenshot2.pixels;
        width = screenshot1.width;
        height = screenshot1.height;
        differentPixels = new boolean[width][height];
        boolean foundDifferentPixels = false;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (pixels1[x][y] != pixels2[x][y]) {
                    foundDifferentPixels = true;
                    differentPixels[x][y] = true;
                }
            }
        }
        noDifferencesFound = !foundDifferentPixels;
    }

    /**
     * Updates and returns this {@code CompareScreenshots} object.
     */
    public CompareScreenshots ignore(Collection<RectangularRegion> ignoredRegions) {
        for (RectangularRegion ignoredRegion : ignoredRegions) {
            int x2 = Math.min(width - 1, ignoredRegion.x2);
            int y2 = Math.min(height- 1, ignoredRegion.y2);
            for (int x = ignoredRegion.x1; x <= x2; ++x) {
                for (int y = ignoredRegion.y1; y <= y2; ++y) {
                    differentPixels[x][y] = false;
                }
            }
        }
        boolean foundDifferentPixels = false;
        outerLoop:
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (differentPixels[x][y]) {
                    foundDifferentPixels = true;
                    break outerLoop;
                }
            }
        }
        noDifferencesFound = !foundDifferentPixels;
        return this;
    }
}
