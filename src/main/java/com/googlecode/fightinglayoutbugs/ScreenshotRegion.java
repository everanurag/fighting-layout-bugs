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

/**
 * Represents a {@link RectangularRegion} in a {@link Screenshot}.
 */
public class ScreenshotRegion {

    public final Screenshot screenshot;
    public final RectangularRegion region;

    public ScreenshotRegion(Screenshot screenshot, RectangularRegion region) {
        if (region.x1 < 0 || region.y1 < 0) {
            throw new IllegalArgumentException("Top left corner of given region " + region + " is not inside the given screenshot.");
        }
        if (region.x2 >= screenshot.width || region.y2 >= screenshot.height) {
            throw new IllegalArgumentException("Bottom right corner of given region " + region + " is not inside the given screenshot of width " + screenshot.width + " pixels and height " + screenshot.height + " pixels.");
        }
        this.screenshot = screenshot;
        this.region = region;
    }

    @Override
    public int hashCode() {
        return region.hashCode() ^ screenshot.pixels[region.x1][region.y1];
    }

    /**
     * Returns {@code true} if {@code o} is a {@code ScreenshotRegion} with
     * exactly the the same top left corner, bottom right corner,
     * and the same pixels within the rectangular region with these corners.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof ScreenshotRegion) {
            ScreenshotRegion other = (ScreenshotRegion) o;
            if (this.region.equals(other.region)) {
                int[][] p1 = screenshot.pixels;
                int[][] p2 = other.screenshot.pixels;
                for (int x = region.x1; x <= region.x2; ++x) {
                    for (int y = region.y1; y <= region.y2; ++y) {
                        if (p1[x][y] != p2[x][y]) {
                            return false;
                        }
                    }
                }
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
