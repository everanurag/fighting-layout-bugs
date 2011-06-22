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

package com.googlecode.fightinglayoutbugs.helpers;

/**
 * @author Michael Tamm
 */
public final class Point {
    public final int x;
    public final int y;

    public Point(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x (" + x + ") must not be negative.");
        }
        if (y < 0) {
            throw new IllegalArgumentException("y (" + y + ") must not be negative.");
        }
        this.x = x;
        this.y = y;
    }

    @Override
    public int hashCode() {
        return (x << 16 | y);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Point) {
            Point other = (Point) o;
            return (this.x == other.x && this.y == other.y);
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return x + "," + y;
    }
}
