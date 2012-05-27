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

import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.assertThat;
import static com.googlecode.fightinglayoutbugs.helpers.HamcrestHelper.is;

@RunWith(Theories.class)
public class AnimationAwareTextDetectorTest extends TestUsingSelenium {

    @DataPoints public static final String[] ALL_WEB_PAGES_TO_TEST = new String[] {
        "/Yahoo!_Profile_Updates.html",
        "/Microsoft_Newsletter.html",
        "/ESPRIT_newsletter.html"
    };

    @Theory
    public void shouldBehaveLikeSimpleTextDetectorWhenThereIsNoAnimation(String path) throws Exception {
        WebPage testPage = getWebPageFor(path);
        boolean[][] expected = new SimpleTextDetector().detectTextPixelsIn(testPage);
        boolean[][] actual = new AnimationAwareTextDetector().detectTextPixelsIn(testPage);
        assertThat(actual, is(expected));
    }

    @Test
    public void shouldIgnoreAnimatedGifImage() throws Exception {
        WebPage testPage = getWebPageFor("/page_with_animated_gif.html");
        final TextDetector detector = new AnimationAwareTextDetector();
        final boolean[][] textPixels = detector.detectTextPixelsIn(testPage);
        for (boolean[] column : textPixels) {
            for (boolean isTextPixel : column) {
                assertThat(isTextPixel, is(false));
            }
        }
    }

}
