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

package com.googlecode.fightinglayoutbugs;

/**
 * Detects pixels, which belong to textual content in a web page.
 *
 * @author Michael Tamm
 */
public interface TextDetector {

    /**
     * Call this method to configure the text detector in such a way, that all rectangle
     * regions of the analyzed web page covered by those elements, which are selected
     * by the given jQuery selector, will be ignored when text pixels are detected.
     */
    void ignore(String jQuerySelector);

    /**
     * Returns a two dimensional array <tt>a</tt>, whereby <tt>a[x][y]</tt> is <tt>true</tt>
     * if the pixel with the coordinates x,y in a {@link #getScreenshot screenshot} of this web page
     * belongs to displayed text, otherwise <tt>a[x][y]</tt> is <tt>false</tt>.
     */
    boolean[][] detectTextPixelsIn(WebPage webpage);

}
