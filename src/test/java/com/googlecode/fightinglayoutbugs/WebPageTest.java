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
import org.junit.Test;

import java.util.Collection;

import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.assertThat;
import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.is;
import static com.googlecode.fightinglayoutbugs.helpers.TestHelper.asList;

public class WebPageTest extends TestUsingSelenium {

    @Test
    public void testGetRectangularRegions() {
        WebPage pageWithFlashMovie = getWebPageFor("/page_with_flash_movie.html");
        Collection<RectangularRegion> rectangularRegions = pageWithFlashMovie.getRectangularRegionsCoveredBy(asList("embed"));
        assertThat(rectangularRegions.size(), is(1));
        RectangularRegion flashMovie = rectangularRegions.iterator().next();
        assertThat(flashMovie.x1, is(50));
        assertThat(flashMovie.y1, is(110));
        assertThat(flashMovie.x2, is(50 + 793 - 1));
        assertThat(flashMovie.y2, is(110 + 225 - 1));
    }

}
