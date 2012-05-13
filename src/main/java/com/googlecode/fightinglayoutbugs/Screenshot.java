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

import com.googlecode.fightinglayoutbugs.helpers.Dimension;
import com.googlecode.fightinglayoutbugs.helpers.ImageHelper;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Represents a screenshot of an entire {@link WebPage}.
 */
public class Screenshot implements Serializable {

    private static final long serialVersionUID = 2L;

    public int[][] pixels;
    public final int width;
    public final int height;
    public final Dimension dimension;

    @edu.umd.cs.findbugs.annotations.SuppressWarnings("EI2")
    public Screenshot(int[][] pixels) {
        this.pixels = pixels;
        width = pixels.length;
        height = pixels[0].length;
        dimension = new Dimension(width, height);
    }

    public BufferedImage toBufferedImage() {
        return ImageHelper.pixelsToImage(pixels);
    }
}
