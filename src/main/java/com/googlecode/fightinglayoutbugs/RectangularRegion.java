/*
 * Copyright 2009-2011 Michael Tamm
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

/**
 * @author Michael Tamm
 */
public class RectangularRegion {

    public final int x1;
    public final int y1;
    /** inclusive */
    public final int x2;
    /** inclusive */
    public final int y2;

    /**
     * Constructs a rectangular region covering all
     * pixels from the top left corner (x1,x2)
     * to the right bottom corner (x2,y2).
     */
    public RectangularRegion(int x1, int y1, int x2, int y2) {
        if (x1 < 0) {
            throw new IllegalArgumentException("x1 (" + x1 + ") must not be negative.");
        }
        if (y1 < 0) {
            throw new IllegalArgumentException("y1 (" + y1 + ") must not be negative.");
        }
        if (x2 < x1) {
            throw new IllegalArgumentException("x2 (" + x2 + ") must not be less than x1 (" + x1 + ").");
        }
        if (y2 < y1) {
            throw new IllegalArgumentException("y2 (" + y2 + ") must not be less than y1 (" + y1 + ").");
        }
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    }
}
