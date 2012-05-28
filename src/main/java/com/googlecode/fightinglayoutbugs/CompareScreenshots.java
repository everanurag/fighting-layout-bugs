/*
 * Copyright 2009-2012 Michael Tamm
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

import com.googlecode.fightinglayoutbugs.helpers.RectangularRegion;

import java.util.Collection;

public class CompareScreenshots {

    public final int width;
    public final int height;
    public final boolean[][] differentPixels;

    private boolean foundDifferences;

    public CompareScreenshots(Screenshot screenshot1, Screenshot screenshot2) {
        int[][] pixels1 = screenshot1.pixels;
        int[][] pixels2 = screenshot2.pixels;
        width = Math.min(screenshot1.width, screenshot2.width);
        height = Math.min(screenshot1.height, screenshot2.height);
        differentPixels = new boolean[width][height];
        foundDifferences = false;
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (pixels1[x][y] != pixels2[x][y]) {
                    foundDifferences = true;
                    differentPixels[x][y] = true;
                }
            }
        }
    }

    /**
     * Updates and returns this {@code CompareScreenshots} object.
     */
    public CompareScreenshots ignore(Collection<RectangularRegion> regionsToIgnore) {
        for (RectangularRegion ignoredRegion : regionsToIgnore) {
            int x2 = Math.min(width - 1, ignoredRegion.x2);
            int y2 = Math.min(height - 1, ignoredRegion.y2);
            for (int x = ignoredRegion.x1; x <= x2; ++x) {
                for (int y = ignoredRegion.y1; y <= y2; ++y) {
                    differentPixels[x][y] = false;
                }
            }
        }
        foundDifferences = false;
        outerLoop:
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                if (differentPixels[x][y]) {
                    foundDifferences = true;
                    break outerLoop;
                }
            }
        }
        return this;
    }

    public boolean noDifferencesFound() {
        return !foundDifferences;
    }

    public boolean differencesFound() {
        return foundDifferences;
    }
}
